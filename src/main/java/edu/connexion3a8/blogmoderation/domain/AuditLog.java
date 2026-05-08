package edu.connexion3a8.blogmoderation.domain;

import java.time.LocalDateTime;

public class AuditLog {
    private final long reportId;
    private final String actor;
    private final String action;
    private final String details;
    private final LocalDateTime createdAt;

    public AuditLog(long reportId, String actor, String action, String details, LocalDateTime createdAt) {
        this.reportId = reportId;
        this.actor = actor;
        this.action = action;
        this.details = details;
        this.createdAt = createdAt;
    }

    public long getReportId() { return reportId; }
    public String getActor() { return actor; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
