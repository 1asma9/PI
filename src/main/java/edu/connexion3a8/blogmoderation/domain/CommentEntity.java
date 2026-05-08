package edu.connexion3a8.blogmoderation.domain;

import java.time.LocalDateTime;

public class CommentEntity {
    private long id;
    private long blogId;
    private String author;
    private String content;
    private boolean visible;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;

    public CommentEntity(long id, long blogId, String author, String content, boolean visible,
                         LocalDateTime createdAt, LocalDateTime updatedAt, long version) {
        this.id = id;
        this.blogId = blogId;
        this.author = author;
        this.content = content;
        this.visible = visible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.version = version;
    }

    public long getId() { return id; }
    public long getBlogId() { return blogId; }
    public String getAuthor() { return author; }
    public String getContent() { return content; }
    public boolean isVisible() { return visible; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public long getVersion() { return version; }
    public void setVisible(boolean visible) { this.visible = visible; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setVersion(long version) { this.version = version; }
}
