package edu.connexion3a8.blogmoderation.domain;

import java.time.LocalDateTime;

public class CommentReport {
    private long id;
    private long commentId;
    private String commentAuthor;
    private String contentSnapshot;
    private int reportCount;
    private ReportReason mainReason;
    private PriorityLevel priority;
    private ReportStatus status;
    private String assignedModerator;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime firstReviewAt;
    private LocalDateTime resolvedAt;
    private long version;

    public CommentReport(long id, long commentId, String commentAuthor, String contentSnapshot, int reportCount,
                         ReportReason mainReason, PriorityLevel priority, ReportStatus status, String assignedModerator,
                         LocalDateTime createdAt, LocalDateTime updatedAt, long version) {
        this.id = id;
        this.commentId = commentId;
        this.commentAuthor = commentAuthor;
        this.contentSnapshot = contentSnapshot;
        this.reportCount = reportCount;
        this.mainReason = mainReason;
        this.priority = priority;
        this.status = status;
        this.assignedModerator = assignedModerator;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public long getId() { return id; }
    public long getCommentId() { return commentId; }
    public String getCommentAuthor() { return commentAuthor; }
    public String getContentSnapshot() { return contentSnapshot; }
    public int getReportCount() { return reportCount; }
    public ReportReason getMainReason() { return mainReason; }
    public PriorityLevel getPriority() { return priority; }
    public ReportStatus getStatus() { return status; }
    public String getAssignedModerator() { return assignedModerator; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public LocalDateTime getFirstReviewAt() { return firstReviewAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public long getVersion() { return version; }

    public void setPriority(PriorityLevel priority) { this.priority = priority; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public void setAssignedModerator(String assignedModerator) { this.assignedModerator = assignedModerator; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setFirstReviewAt(LocalDateTime firstReviewAt) { this.firstReviewAt = firstReviewAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public void setVersion(long version) { this.version = version; }
}
