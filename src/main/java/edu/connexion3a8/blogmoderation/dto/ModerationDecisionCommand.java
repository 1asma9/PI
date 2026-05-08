package edu.connexion3a8.blogmoderation.dto;

import edu.connexion3a8.blogmoderation.domain.ModerationAction;

public class ModerationDecisionCommand {
    private final long reportId;
    private final long expectedVersion;
    private final ModerationAction action;
    private final String moderator;
    private final String reason;
    private final String note;

    public ModerationDecisionCommand(long reportId, long expectedVersion, ModerationAction action, String moderator, String reason, String note) {
        this.reportId = reportId;
        this.expectedVersion = expectedVersion;
        this.action = action;
        this.moderator = moderator;
        this.reason = reason;
        this.note = note;
    }

    public long getReportId() { return reportId; }
    public long getExpectedVersion() { return expectedVersion; }
    public ModerationAction getAction() { return action; }
    public String getModerator() { return moderator; }
    public String getReason() { return reason; }
    public String getNote() { return note; }
}
