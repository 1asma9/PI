package edu.connexion3a8.blogmoderation.service;

import edu.connexion3a8.blogmoderation.domain.*;
import edu.connexion3a8.blogmoderation.dto.*;
import edu.connexion3a8.blogmoderation.repository.ModerationRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class MockModerationDashboardService implements ModerationDashboardService {
    private final ModerationRepository repository;
    private final int slaPendingHours;

    public MockModerationDashboardService(ModerationRepository repository) {
        this(repository, 6);
    }

    public MockModerationDashboardService(ModerationRepository repository, int slaPendingHours) {
        this.repository = repository;
        this.slaPendingHours = slaPendingHours;
    }

    @Override
    public List<ReportRowViewModel> search(ModerationFilter filter, int page, int size) {
        return applyFilter(filter).stream()
                .sorted(this::compareByUrgency)
                .skip((long) page * size)
                .limit(size)
                .map(this::toRow)
                .collect(Collectors.toList());
    }

    @Override
    public int count(ModerationFilter filter) {
        return applyFilter(filter).size();
    }

    @Override
    public ModerationDetailDto getDetail(long reportId) {
        CommentReport report = repository.findReportById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        ModerationAnalysis analysis = repository.findAnalysisByReportId(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Analysis not found: " + reportId));
        return new ModerationDetailDto(report, analysis, repository.findAuditByReportId(reportId));
    }

    @Override
    public ModerationKpi computeKpi() {
        List<CommentReport> all = repository.findAllReports();
        long pending = all.stream().filter(r -> r.getStatus() == ReportStatus.PENDING).count();
        long escalated = all.stream().filter(r -> r.getStatus() == ReportStatus.ESCALATED).count();
        long approved = all.stream().filter(r -> r.getStatus() == ReportStatus.APPROVED).count();
        long resolved = all.stream().filter(r -> r.getResolvedAt() != null).count();
        double approvalRate = resolved == 0 ? 0 : (approved * 100.0 / resolved);

        double avgHours = all.stream()
                .filter(r -> r.getResolvedAt() != null)
                .mapToLong(r -> Duration.between(r.getCreatedAt(), r.getResolvedAt()).toHours())
                .average()
                .orElse(0);

        long compared = 0;
        long accurate = 0;
        for (CommentReport r : all) {
            ModerationAnalysis a = repository.findAnalysisByReportId(r.getId()).orElse(null);
            if (a == null) continue;
            if (isFinalStatus(r.getStatus())) {
                compared++;
                if (mapsRecommendationToStatus(a.getRecommendation()) == r.getStatus()) accurate++;
            }
        }
        double aiPrecision = compared == 0 ? 0 : (accurate * 100.0 / compared);
        return new ModerationKpi(avgHours, approvalRate, pending, escalated, aiPrecision);
    }

    @Override
    public void decide(ModerationDecisionCommand command, UserRole actorRole) {
        if (!ModerationRules.canExecute(actorRole, command.getAction())) {
            throw new SecurityException("Action not allowed for role: " + actorRole);
        }
        if (ModerationRules.requiresMandatoryReason(command.getAction()) &&
                (command.getReason() == null || command.getReason().isBlank())) {
            throw new IllegalArgumentException("Reason is required for action " + command.getAction());
        }

        CommentReport report = repository.findReportById(command.getReportId())
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if (report.getVersion() != command.getExpectedVersion()) {
            throw new IllegalStateException("Concurrent modification detected");
        }

        ReportStatus from = report.getStatus();
        if (from == ReportStatus.PENDING) {
            transit(report, ReportStatus.IN_REVIEW, command.getModerator(), "AUTO_REVIEW_START", "Transition auto vers IN_REVIEW");
            from = report.getStatus();
        }

        ReportStatus target = mapActionToStatus(command.getAction());
        if (!ModerationStateMachine.isValidTransition(from, target)) {
            throw new IllegalStateException("Invalid state transition from " + from + " to " + target);
        }

        transit(report, target, command.getModerator(), command.getAction().name(),
                command.getReason() == null ? command.getNote() : command.getReason());
    }

    @Override
    public void bulkDecide(List<ModerationDecisionCommand> commands, UserRole actorRole) {
        for (ModerationDecisionCommand command : commands) {
            decide(command, actorRole);
        }
    }

    @Override
    public void reassign(long reportId, long expectedVersion, String moderator, String actor) {
        CommentReport report = repository.findReportById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        if (report.getVersion() != expectedVersion) {
            throw new IllegalStateException("Concurrent modification detected");
        }
        report.setAssignedModerator(moderator);
        report.setUpdatedAt(LocalDateTime.now());
        report.setVersion(report.getVersion() + 1);
        repository.saveReport(report);
        repository.appendAudit(new AuditLog(reportId, actor, "REASSIGN", "Reassigne a " + moderator, LocalDateTime.now()));
    }

    @Override
    public void addInternalNote(long reportId, String actor, String note) {
        repository.appendAudit(new AuditLog(reportId, actor, "INTERNAL_NOTE", note, LocalDateTime.now()));
    }

    @Override
    public List<String> listModerators() {
        return List.of("moderator1", "moderator2", "senior.moderator", "admin.main");
    }

    private List<CommentReport> applyFilter(ModerationFilter filter) {
        String query = filter.getQuery() == null ? "" : filter.getQuery().trim().toLowerCase(Locale.ROOT);
        return repository.findAllReports().stream()
                .filter(r -> filter.getStatus() == null || r.getStatus() == filter.getStatus())
                .filter(r -> filter.getPriority() == null || r.getPriority() == filter.getPriority())
                .filter(r -> filter.getReason() == null || r.getMainReason() == filter.getReason())
                .filter(r -> !filter.isOnlyPending() || r.getStatus() == ReportStatus.PENDING)
                .filter(r -> filter.getFromDate() == null || !r.getCreatedAt().toLocalDate().isBefore(filter.getFromDate()))
                .filter(r -> filter.getToDate() == null || !r.getCreatedAt().toLocalDate().isAfter(filter.getToDate()))
                .filter(r -> query.isBlank() || r.getCommentAuthor().toLowerCase(Locale.ROOT).contains(query)
                        || r.getContentSnapshot().toLowerCase(Locale.ROOT).contains(query))
                .collect(Collectors.toList());
    }

    private int compareByUrgency(CommentReport left, CommentReport right) {
        int p = Integer.compare(weight(right.getPriority()), weight(left.getPriority()));
        if (p != 0) return p;
        boolean leftSla = isSlaBreached(left);
        boolean rightSla = isSlaBreached(right);
        if (leftSla != rightSla) return Boolean.compare(rightSla, leftSla);
        return right.getCreatedAt().compareTo(left.getCreatedAt());
    }

    private int weight(PriorityLevel level) {
        return switch (level) {
            case CRITICAL -> 4;
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private boolean isSlaBreached(CommentReport report) {
        return report.getStatus() == ReportStatus.PENDING &&
                Duration.between(report.getCreatedAt(), LocalDateTime.now()).toHours() >= slaPendingHours;
    }

    private ReportRowViewModel toRow(CommentReport report) {
        ModerationAnalysis analysis = repository.findAnalysisByReportId(report.getId()).orElse(null);
        ReportRowViewModel row = new ReportRowViewModel();
        row.setReportId(report.getId());
        row.setCommentId(report.getCommentId());
        row.setAuthor(report.getCommentAuthor());
        row.setPreview(truncate(report.getContentSnapshot(), 50));
        row.setReportCount(report.getReportCount());
        row.setReason(report.getMainReason());
        row.setToxicity(analysis == null ? 0 : analysis.getToxicityScore());
        row.setPriority(report.getPriority());
        row.setStatus(report.getStatus());
        row.setCreatedAt(report.getCreatedAt());
        row.setUpdatedAt(report.getUpdatedAt());
        row.setAssignedModerator(report.getAssignedModerator());
        row.setVersion(report.getVersion());
        return row;
    }

    private void transit(CommentReport report, ReportStatus target, String actor, String action, String detail) {
        ReportStatus from = report.getStatus();
        if (!ModerationStateMachine.isValidTransition(from, target)) {
            throw new IllegalStateException("Invalid state transition from " + from + " to " + target);
        }
        LocalDateTime now = LocalDateTime.now();
        if (report.getFirstReviewAt() == null && target == ReportStatus.IN_REVIEW) {
            report.setFirstReviewAt(now);
        }
        if (isFinalStatus(target)) {
            report.setResolvedAt(now);
        }
        report.setStatus(target);
        report.setUpdatedAt(now);
        report.setVersion(report.getVersion() + 1);
        repository.saveReport(report);
        repository.appendAudit(new AuditLog(report.getId(), actor, action, detail, now));
    }

    private ReportStatus mapActionToStatus(ModerationAction action) {
        return switch (action) {
            case APPROVE -> ReportStatus.APPROVED;
            case HIDE -> ReportStatus.HIDDEN;
            case DELETE -> ReportStatus.DELETED;
            case ESCALATE -> ReportStatus.ESCALATED;
        };
    }

    private ReportStatus mapsRecommendationToStatus(ModerationAction recommendation, boolean allowAuto) {
        return allowAuto ? mapActionToStatus(recommendation) : ReportStatus.IN_REVIEW;
    }

    private ReportStatus mapsRecommendationToStatus(ModerationAction recommendation) {
        return mapsRecommendationToStatus(recommendation, true);
    }

    private boolean isFinalStatus(ReportStatus status) {
        return status == ReportStatus.APPROVED
                || status == ReportStatus.HIDDEN
                || status == ReportStatus.DELETED
                || status == ReportStatus.ESCALATED;
    }

    private String truncate(String text, int max) {
        if (text == null || text.length() <= max) return text;
        return text.substring(0, max - 1) + "...";
    }
}
