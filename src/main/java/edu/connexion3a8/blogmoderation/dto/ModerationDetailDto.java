package edu.connexion3a8.blogmoderation.dto;

import edu.connexion3a8.blogmoderation.domain.AuditLog;
import edu.connexion3a8.blogmoderation.domain.CommentReport;
import edu.connexion3a8.blogmoderation.domain.ModerationAnalysis;

import java.util.List;

public class ModerationDetailDto {
    private final CommentReport report;
    private final ModerationAnalysis analysis;
    private final List<AuditLog> history;

    public ModerationDetailDto(CommentReport report, ModerationAnalysis analysis, List<AuditLog> history) {
        this.report = report;
        this.analysis = analysis;
        this.history = history;
    }

    public CommentReport getReport() { return report; }
    public ModerationAnalysis getAnalysis() { return analysis; }
    public List<AuditLog> getHistory() { return history; }
}
