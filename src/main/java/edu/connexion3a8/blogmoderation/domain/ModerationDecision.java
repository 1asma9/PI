package edu.connexion3a8.blogmoderation.domain;

import java.time.LocalDateTime;

public class ModerationDecision {
    private final long reportId;
    private final ModerationAction action;
    private final String moderator;
    private final String reason;
    private final String note;
    private final ReportStatus fromStatus;
    private final ReportStatus toStatus;
    private final LocalDateTime createdAt;

    public ModerationDecision(long reportId, ModerationAction action, String moderator, String reason, String note,
                              ReportStatus fromStatus, ReportStatus toStatus, LocalDateTime createdAt) {
        this.reportId = reportId;
        this.action = action;
        this.moderator = moderator;
        this.reason = reason;
        this.note = note;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.createdAt = createdAt;
    }

    public long getReportId() { return reportId; }
    public ModerationAction getAction() { return action; }
    public String getModerator() { return moderator; }
    public String getReason() { return reason; }
    public String getNote() { return note; }
    public ReportStatus getFromStatus() { return fromStatus; }
    public ReportStatus getToStatus() { return toStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
