package org.example.model;

import java.time.LocalDateTime;
import java.util.List;

public class Comment {
    private String id;
    private String content;
    private String authorId;
    private String authorName;
    private String bookId;
    private String parentCommentId;
    private List<Comment> replies;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean isDeleted;
    private int replyCount;
    private boolean hasReplies;
    private boolean isTopLevel;
    private int depth;

    // Constructors
    public Comment() {}

    public Comment(String content, String authorId, String bookId) {
        this.content = content;
        this.authorId = authorId;
        this.bookId = bookId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isDeleted = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getParentCommentId() { return parentCommentId; }
    public void setParentCommentId(String parentCommentId) { this.parentCommentId = parentCommentId; }

    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public int getReplyCount() { return replyCount; }
    public void setReplyCount(int replyCount) { this.replyCount = replyCount; }

    public boolean isHasReplies() { return hasReplies; }
    public void setHasReplies(boolean hasReplies) { this.hasReplies = hasReplies; }

    public boolean isTopLevel() { return isTopLevel; }
    public void setTopLevel(boolean topLevel) { isTopLevel = topLevel; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    // Helper methods
    public boolean hasReplies() {
        return replies != null && !replies.isEmpty();
    }

    public String getFormattedCreatedAt() {
        if (createdAt != null) {
            return createdAt.toString().replace('T', ' ').substring(0, 19);
        }
        return "";
    }

    public String getFormattedUpdatedAt() {
        if (updatedAt != null) {
            return updatedAt.toString().replace('T', ' ').substring(0, 19);
        }
        return "";
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id='" + id + '\'' +
                ", content='" + content + '\'' +
                ", authorName='" + authorName + '\'' +
                ", createdAt=" + createdAt +
                ", replyCount=" + replyCount +
                '}';
    }
}
