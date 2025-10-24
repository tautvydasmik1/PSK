package org.example.web.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    @JsonIgnore
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonIgnore
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    @JsonIgnore
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Comment> replies = new ArrayList<>();

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private boolean isDeleted = false;

    // Constructors
    public Comment() {}

    public Comment(String content, User author, Book book) {
        this.content = content;
        this.author = author;
        this.book = book;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Comment(String content, User author, Book book, Comment parentComment) {
        this(content, author, book);
        this.parentComment = parentComment;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Comment getParentComment() { return parentComment; }
    public void setParentComment(Comment parentComment) { this.parentComment = parentComment; }

    public List<Comment> getReplies() { return replies; }
    public void setReplies(List<Comment> replies) { this.replies = replies; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods for JSON serialization
    public String getAuthorId() {
        return author != null ? author.getId() : null;
    }

    public String getAuthorName() {
        return author != null ? author.getFullName() : null;
    }

    public String getBookId() {
        return book != null ? book.getId() : null;
    }

    public String getParentCommentId() {
        return parentComment != null ? parentComment.getId() : null;
    }

    public int getReplyCount() {
        return replies != null ? replies.size() : 0;
    }

    public boolean hasReplies() {
        return replies != null && !replies.isEmpty();
    }

    // Method to add a reply
    public void addReply(Comment reply) {
        if (replies == null) {
            replies = new ArrayList<>();
        }
        replies.add(reply);
        reply.setParentComment(this);
    }

    // Method to remove a reply
    public void removeReply(Comment reply) {
        if (replies != null) {
            replies.remove(reply);
            reply.setParentComment(null);
        }
    }

    // Method to check if this is a top-level comment
    public boolean isTopLevel() {
        return parentComment == null;
    }

    // Method to get the depth level of this comment
    public int getDepth() {
        if (parentComment == null) {
            return 0;
        }
        return parentComment.getDepth() + 1;
    }
}
