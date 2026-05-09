package edu.connexion3a8.blogmoderation.dto;

import edu.connexion3a8.blogmoderation.domain.PriorityLevel;
import edu.connexion3a8.blogmoderation.domain.ReportReason;
import edu.connexion3a8.blogmoderation.domain.ReportStatus;

import java.time.LocalDate;

public class ModerationFilter {
    private ReportStatus status;
    private PriorityLevel priority;
    private ReportReason reason;
    private LocalDate fromDate;
    private LocalDate toDate;
    private boolean onlyPending;
    private String query;

    public ReportStatus getStatus() { return status; }
    public PriorityLevel getPriority() { return priority; }
    public ReportReason getReason() { return reason; }
    public LocalDate getFromDate() { return fromDate; }
    public LocalDate getToDate() { return toDate; }
    public boolean isOnlyPending() { return onlyPending; }
    public String getQuery() { return query; }
    public void setStatus(ReportStatus status) { this.status = status; }
    public void setPriority(PriorityLevel priority) { this.priority = priority; }
    public void setReason(ReportReason reason) { this.reason = reason; }
    public void setFromDate(LocalDate fromDate) { this.fromDate = fromDate; }
    public void setToDate(LocalDate toDate) { this.toDate = toDate; }
    public void setOnlyPending(boolean onlyPending) { this.onlyPending = onlyPending; }
    public void setQuery(String query) { this.query = query; }
}
