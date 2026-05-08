package edu.connexion3a8.blogmoderation.dto;

import edu.connexion3a8.blogmoderation.domain.PriorityLevel;
import edu.connexion3a8.blogmoderation.domain.ReportReason;
import edu.connexion3a8.blogmoderation.domain.ReportStatus;
import javafx.beans.property.*;

import java.time.LocalDateTime;

public class ReportRowViewModel {
    private final LongProperty reportId = new SimpleLongProperty();
    private final LongProperty commentId = new SimpleLongProperty();
    private final StringProperty author = new SimpleStringProperty();
    private final StringProperty preview = new SimpleStringProperty();
    private final IntegerProperty reportCount = new SimpleIntegerProperty();
    private final ObjectProperty<ReportReason> reason = new SimpleObjectProperty<>();
    private final IntegerProperty toxicity = new SimpleIntegerProperty();
    private final ObjectProperty<PriorityLevel> priority = new SimpleObjectProperty<>();
    private final ObjectProperty<ReportStatus> status = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();
    private final StringProperty assignedModerator = new SimpleStringProperty();
    private final LongProperty version = new SimpleLongProperty();

    public long getReportId() { return reportId.get(); }
    public LongProperty reportIdProperty() { return reportId; }
    public void setReportId(long value) { reportId.set(value); }
    public long getCommentId() { return commentId.get(); }
    public LongProperty commentIdProperty() { return commentId; }
    public void setCommentId(long value) { commentId.set(value); }
    public String getAuthor() { return author.get(); }
    public StringProperty authorProperty() { return author; }
    public void setAuthor(String value) { author.set(value); }
    public String getPreview() { return preview.get(); }
    public StringProperty previewProperty() { return preview; }
    public void setPreview(String value) { preview.set(value); }
    public int getReportCount() { return reportCount.get(); }
    public IntegerProperty reportCountProperty() { return reportCount; }
    public void setReportCount(int value) { reportCount.set(value); }
    public ReportReason getReason() { return reason.get(); }
    public ObjectProperty<ReportReason> reasonProperty() { return reason; }
    public void setReason(ReportReason value) { reason.set(value); }
    public int getToxicity() { return toxicity.get(); }
    public IntegerProperty toxicityProperty() { return toxicity; }
    public void setToxicity(int value) { toxicity.set(value); }
    public PriorityLevel getPriority() { return priority.get(); }
    public ObjectProperty<PriorityLevel> priorityProperty() { return priority; }
    public void setPriority(PriorityLevel value) { priority.set(value); }
    public ReportStatus getStatus() { return status.get(); }
    public ObjectProperty<ReportStatus> statusProperty() { return status; }
    public void setStatus(ReportStatus value) { status.set(value); }
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public void setCreatedAt(LocalDateTime value) { createdAt.set(value); }
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime value) { updatedAt.set(value); }
    public String getAssignedModerator() { return assignedModerator.get(); }
    public StringProperty assignedModeratorProperty() { return assignedModerator; }
    public void setAssignedModerator(String value) { assignedModerator.set(value); }
    public long getVersion() { return version.get(); }
    public LongProperty versionProperty() { return version; }
    public void setVersion(long value) { version.set(value); }
}
