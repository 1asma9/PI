package edu.connexion3a8.blogmoderation.repository;

import edu.connexion3a8.blogmoderation.domain.AuditLog;
import edu.connexion3a8.blogmoderation.domain.CommentReport;
import edu.connexion3a8.blogmoderation.domain.ModerationAnalysis;

import java.util.List;
import java.util.Optional;

public interface ModerationRepository {
    List<CommentReport> findAllReports();
    Optional<CommentReport> findReportById(long reportId);
    Optional<ModerationAnalysis> findAnalysisByReportId(long reportId);
    List<AuditLog> findAuditByReportId(long reportId);
    void saveReport(CommentReport report);
    void saveAnalysis(long reportId, ModerationAnalysis analysis);
    void appendAudit(AuditLog auditLog);
}
