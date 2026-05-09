package edu.connexion3a8.blogmoderation.repository;

import edu.connexion3a8.blogmoderation.domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryModerationRepository implements ModerationRepository {
    private final Map<Long, CommentReport> reports = new ConcurrentHashMap<>();
    private final Map<Long, ModerationAnalysis> analyses = new ConcurrentHashMap<>();
    private final Map<Long, List<AuditLog>> history = new ConcurrentHashMap<>();

    public InMemoryModerationRepository() {
        seed();
    }

    private void seed() {
        LocalDateTime now = LocalDateTime.now();
        add(
                new CommentReport(1001, 9001, "aziz.b", "Vous etes tous incompetents et nuls...", 7, ReportReason.INSULT,
                        PriorityLevel.HIGH, ReportStatus.PENDING, "moderator1", now.minusHours(8), now.minusHours(1), 1),
                new ModerationAnalysis(78, false, List.of("insult", "harassment"), List.of("incompetents", "nuls"),
                        "fr", 0.92, ModerationAction.HIDE, "Langage agressif repete"),
                List.of(new AuditLog(1001, "system", "REPORT_CREATED", "Signalement agrege", now.minusHours(8)))
        );
        add(
                new CommentReport(1002, 9002, "guest_11", "Je vais te retrouver ce soir...", 3, ReportReason.THREAT,
                        PriorityLevel.CRITICAL, ReportStatus.IN_REVIEW, "senior.moderator", now.minusHours(2), now.minusMinutes(45), 3),
                new ModerationAnalysis(93, true, List.of("threat"), List.of("retrouver"), "fr", 0.97,
                        ModerationAction.DELETE, "Menace explicite detectee"),
                List.of(new AuditLog(1002, "moderator1", "TAKEN_IN_REVIEW", "Pris en charge", now.minusHours(2)))
        );
        add(
                new CommentReport(1003, 9003, "karim_x", "Tres bon article merci!", 2, ReportReason.OTHER,
                        PriorityLevel.LOW, ReportStatus.PENDING, "moderator2", now.minusHours(1), now.minusHours(1), 1),
                new ModerationAnalysis(12, false, List.of("neutral"), List.of(), "fr", 0.83,
                        ModerationAction.APPROVE, "Aucun signal toxique fort"),
                List.of(new AuditLog(1003, "system", "REPORT_CREATED", "Signalements utilisateurs", now.minusHours(1)))
        );
        add(
                new CommentReport(1004, 9004, "botpromo", "Gagnez 1000 EUR en 1h cliquez ici", 11, ReportReason.SPAM,
                        PriorityLevel.HIGH, ReportStatus.PENDING, "moderator1", now.minusHours(14), now.minusHours(5), 5),
                new ModerationAnalysis(73, false, List.of("spam"), List.of("cliquez", "1000 EUR"), "fr", 0.91,
                        ModerationAction.DELETE, "Pattern de spam massif"),
                List.of(new AuditLog(1004, "system", "REPORT_CREATED", "Score spam eleve", now.minusHours(14)))
        );
    }

    private void add(CommentReport report, ModerationAnalysis analysis, List<AuditLog> logs) {
        reports.put(report.getId(), report);
        analyses.put(report.getId(), analysis);
        history.put(report.getId(), new ArrayList<>(logs));
    }

    @Override
    public List<CommentReport> findAllReports() {
        return new ArrayList<>(reports.values());
    }

    @Override
    public Optional<CommentReport> findReportById(long reportId) {
        return Optional.ofNullable(reports.get(reportId));
    }

    @Override
    public Optional<ModerationAnalysis> findAnalysisByReportId(long reportId) {
        return Optional.ofNullable(analyses.get(reportId));
    }

    @Override
    public List<AuditLog> findAuditByReportId(long reportId) {
        return new ArrayList<>(history.getOrDefault(reportId, List.of()));
    }

    @Override
    public void saveReport(CommentReport report) {
        reports.put(report.getId(), report);
    }

    @Override
    public void saveAnalysis(long reportId, ModerationAnalysis analysis) {
        analyses.put(reportId, analysis);
    }

    @Override
    public void appendAudit(AuditLog auditLog) {
        history.computeIfAbsent(auditLog.getReportId(), k -> new ArrayList<>()).add(auditLog);
    }
}
