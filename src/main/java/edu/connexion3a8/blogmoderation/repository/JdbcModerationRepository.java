package edu.connexion3a8.blogmoderation.repository;

import edu.connexion3a8.blogmoderation.domain.*;
import edu.connexion3a8.blogmoderation.service.ModerationRules;
import edu.connexion3a8.services.ModerationService;
import edu.connexion3a8.tools.MyConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcModerationRepository implements ModerationRepository {
    private final Connection cnx;
    private final ModerationService moderationService;

    public JdbcModerationRepository() {
        this.cnx = MyConnection.getInstance().getCnx();
        this.moderationService = new ModerationService();
        ensureSchema();
    }

    private void ensureSchema() {
        try (Statement st = cnx.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS moderation_analysis (
                  report_id BIGINT PRIMARY KEY,
                  toxicity_score INT NOT NULL,
                  explicit_threat BOOLEAN NOT NULL DEFAULT FALSE,
                  categories TEXT,
                  sensitive_words TEXT,
                  language VARCHAR(16) NOT NULL DEFAULT 'fr',
                  confidence DOUBLE NOT NULL DEFAULT 0.5,
                  recommendation VARCHAR(16) NOT NULL DEFAULT 'APPROVE',
                  justification TEXT,
                  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """);
            st.execute("""
                CREATE TABLE IF NOT EXISTS moderation_audit (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  report_id BIGINT NOT NULL,
                  actor VARCHAR(128) NOT NULL,
                  action VARCHAR(64) NOT NULL,
                  details TEXT,
                  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to initialize moderation schema", e);
        }
    }

    @Override
    public List<CommentReport> findAllReports() {
        String sql = """
            SELECT
              MIN(cr.id) AS report_id,
              cr.commentaire_id,
              c.nomuser,
              c.contenu,
              COUNT(*) AS report_count,
              SUBSTRING_INDEX(GROUP_CONCAT(cr.reason ORDER BY cr.created_at DESC), ',', 1) AS main_reason,
              SUBSTRING_INDEX(GROUP_CONCAT(cr.status ORDER BY cr.updated_at DESC), ',', 1) AS current_status,
              MIN(cr.created_at) AS created_at,
              MAX(COALESCE(cr.updated_at, cr.created_at)) AS updated_at
            FROM comment_report cr
            JOIN commentaire c ON c.id = cr.commentaire_id
            GROUP BY cr.commentaire_id, c.nomuser, c.contenu
            ORDER BY updated_at DESC
            """;
        List<CommentReport> reports = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long reportId = rs.getLong("report_id");
                long commentId = rs.getLong("commentaire_id");
                String content = rs.getString("contenu");
                ModerationAnalysis analysis = findAnalysisByReportId(reportId).orElseGet(() -> {
                    ModerationAnalysis a = analyze(content);
                    saveAnalysis(reportId, a);
                    return a;
                });
                int reportCount = rs.getInt("report_count");
                PriorityLevel priority = ModerationRules.resolvePriority(
                        analysis.getToxicityScore(),
                        analysis.isExplicitThreat(),
                        reportCount
                );
                CommentReport report = new CommentReport(
                        reportId,
                        commentId,
                        rs.getString("nomuser"),
                        content,
                        reportCount,
                        mapReason(rs.getString("main_reason")),
                        priority,
                        mapStatus(rs.getString("current_status")),
                        "moderator1",
                        toDateTime(rs.getTimestamp("created_at")),
                        toDateTime(rs.getTimestamp("updated_at")),
                        reportCount
                );
                reports.add(report);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch reports", e);
        }
        return reports;
    }

    @Override
    public Optional<CommentReport> findReportById(long reportId) {
        return findAllReports().stream().filter(r -> r.getId() == reportId).findFirst();
    }

    @Override
    public Optional<ModerationAnalysis> findAnalysisByReportId(long reportId) {
        String sql = "SELECT * FROM moderation_analysis WHERE report_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(new ModerationAnalysis(
                        rs.getInt("toxicity_score"),
                        rs.getBoolean("explicit_threat"),
                        splitCsv(rs.getString("categories")),
                        splitCsv(rs.getString("sensitive_words")),
                        rs.getString("language"),
                        rs.getDouble("confidence"),
                        ModerationAction.valueOf(rs.getString("recommendation")),
                        rs.getString("justification")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read analysis", e);
        }
    }

    @Override
    public List<AuditLog> findAuditByReportId(long reportId) {
        String sql = "SELECT report_id, actor, action, details, created_at FROM moderation_audit WHERE report_id = ? ORDER BY created_at DESC";
        List<AuditLog> logs = new ArrayList<>();
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, reportId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    logs.add(new AuditLog(
                            rs.getLong("report_id"),
                            rs.getString("actor"),
                            rs.getString("action"),
                            rs.getString("details"),
                            toDateTime(rs.getTimestamp("created_at"))
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to read audit", e);
        }
        return logs;
    }

    @Override
    public void saveReport(CommentReport report) {
        String status = toDbStatus(report.getStatus());
        String sql = "UPDATE comment_report SET status = ?, updated_at = NOW() WHERE commentaire_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setLong(2, report.getCommentId());
            ps.executeUpdate();
            applyCommentModeration(report);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save report status", e);
        }
    }

    @Override
    public void saveAnalysis(long reportId, ModerationAnalysis analysis) {
        String sql = """
            INSERT INTO moderation_analysis(report_id,toxicity_score,explicit_threat,categories,sensitive_words,language,confidence,recommendation,justification)
            VALUES(?,?,?,?,?,?,?,?,?)
            ON DUPLICATE KEY UPDATE
              toxicity_score=VALUES(toxicity_score),
              explicit_threat=VALUES(explicit_threat),
              categories=VALUES(categories),
              sensitive_words=VALUES(sensitive_words),
              language=VALUES(language),
              confidence=VALUES(confidence),
              recommendation=VALUES(recommendation),
              justification=VALUES(justification)
            """;
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, reportId);
            ps.setInt(2, analysis.getToxicityScore());
            ps.setBoolean(3, analysis.isExplicitThreat());
            ps.setString(4, String.join(",", analysis.getCategories()));
            ps.setString(5, String.join(",", analysis.getSensitiveWords()));
            ps.setString(6, analysis.getLanguage());
            ps.setDouble(7, analysis.getConfidence());
            ps.setString(8, analysis.getRecommendation().name());
            ps.setString(9, analysis.getJustification());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save analysis", e);
        }
    }

    @Override
    public void appendAudit(AuditLog auditLog) {
        String sql = "INSERT INTO moderation_audit(report_id, actor, action, details, created_at) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setLong(1, auditLog.getReportId());
            ps.setString(2, auditLog.getActor());
            ps.setString(3, auditLog.getAction());
            ps.setString(4, auditLog.getDetails());
            ps.setTimestamp(5, Timestamp.valueOf(auditLog.getCreatedAt()));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to append audit", e);
        }
    }

    private ModerationAnalysis analyze(String content) {
        String safe = content == null ? "" : content;
        int badWords = moderationService.countBadWords(safe);
        boolean explicitThreat = safe.toLowerCase().contains("je vais te") || safe.toLowerCase().contains("kill") || safe.toLowerCase().contains("tuer");
        int toxicity = Math.min(100, badWords * 25 + (explicitThreat ? 45 : 0));
        List<String> categories = new ArrayList<>();
        if (badWords > 0) categories.add("insult");
        if (explicitThreat) categories.add("threat");
        if (categories.isEmpty()) categories.add("neutral");
        ModerationAction recommendation = toxicity >= 90 ? ModerationAction.DELETE : toxicity >= 70 ? ModerationAction.HIDE : ModerationAction.APPROVE;
        return new ModerationAnalysis(
                toxicity,
                explicitThreat,
                categories,
                List.of(),
                "fr",
                Math.min(0.99, 0.6 + badWords * 0.08),
                recommendation,
                "auto-analysis from content moderation"
        );
    }

    private void applyCommentModeration(CommentReport report) throws SQLException {
        if (report.getStatus() == ReportStatus.HIDDEN) {
            try (PreparedStatement ps = cnx.prepareStatement("UPDATE commentaire SET contenu = '[Commentaire masqué par modération]' WHERE id = ?")) {
                ps.setLong(1, report.getCommentId());
                ps.executeUpdate();
            }
        } else if (report.getStatus() == ReportStatus.DELETED) {
            try (PreparedStatement ps = cnx.prepareStatement("DELETE FROM commentaire WHERE id = ?")) {
                ps.setLong(1, report.getCommentId());
                ps.executeUpdate();
            }
        }
    }

    private ReportReason mapReason(String dbReason) {
        if (dbReason == null) return ReportReason.OTHER;
        return switch (dbReason.toUpperCase()) {
            case "SPAM" -> ReportReason.SPAM;
            case "INSULT", "INJURE" -> ReportReason.INSULT;
            case "HATE" -> ReportReason.HATE;
            case "HARASSMENT", "HARCELEMENT" -> ReportReason.HARASSMENT;
            case "FAKE_NEWS" -> ReportReason.FAKE_NEWS;
            case "THREAT", "MENACE" -> ReportReason.THREAT;
            default -> ReportReason.OTHER;
        };
    }

    private ReportStatus mapStatus(String dbStatus) {
        if (dbStatus == null) return ReportStatus.PENDING;
        return switch (dbStatus.toUpperCase()) {
            case "PENDING" -> ReportStatus.PENDING;
            case "IN_REVIEW" -> ReportStatus.IN_REVIEW;
            case "APPROVED" -> ReportStatus.APPROVED;
            case "HIDDEN", "REJECTED" -> ReportStatus.HIDDEN;
            case "DELETED" -> ReportStatus.DELETED;
            case "ESCALATED" -> ReportStatus.ESCALATED;
            default -> ReportStatus.PENDING;
        };
    }

    private String toDbStatus(ReportStatus status) {
        return switch (status) {
            case PENDING -> "PENDING";
            case IN_REVIEW -> "IN_REVIEW";
            case APPROVED -> "APPROVED";
            case HIDDEN -> "HIDDEN";
            case DELETED -> "DELETED";
            case ESCALATED -> "ESCALATED";
        };
    }

    private LocalDateTime toDateTime(Timestamp ts) {
        return ts == null ? LocalDateTime.now() : ts.toLocalDateTime();
    }

    private List<String> splitCsv(String input) {
        if (input == null || input.isBlank()) return List.of();
        String[] parts = input.split(",");
        List<String> out = new ArrayList<>();
        for (String p : parts) if (!p.isBlank()) out.add(p.trim());
        return out;
    }
}
