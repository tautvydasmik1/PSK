package org.example.web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private User recipient;



    @NotBlank
    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "is_read")
    private boolean isRead;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_message_id")
    private Message parentMessage;

    @OneToMany(mappedBy = "parentMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> replies = new ArrayList<>();

    private int depth;

    @Column(name = "is_deleted")
    private boolean isDeleted; // Soft delete flag
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt; // When it was deleted



    // Default constructor
    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.depth = 0;
        this.isDeleted = false;
    }

    public Message(Book book, User sender, String content, Message parentMessage) {
        this();
        this.book = book;
        this.sender = sender;
        this.content = content;
        this.parentMessage = parentMessage;
        if (parentMessage != null) {
            this.depth = parentMessage.getDepth() + 1;
        }
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public User getSender() { return sender; }
    public void setSender(User sender) { this.sender = sender; }

    public User getRecipient() { return recipient; }
    public void setRecipient(User recipient) { this.recipient = recipient; }



    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getBookId() {
        return book != null ? book.getId() : null;
    }

    public String getSenderId() {
        return sender != null ? sender.getId() : null;
    }

    public String getRecipientId() {
        return recipient != null ? recipient.getId() : null;
    }

    public String getParentMessageId() {
        return parentMessage != null ? parentMessage.getId() : null;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Message getParentMessage() { return parentMessage; }
    public void setParentMessage(Message parentMessage) { this.parentMessage = parentMessage; }

    public List<Message> getReplies() { return replies; }
    public void setReplies(List<Message> replies) { this.replies = replies; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
        if (deleted) {
            this.deletedAt = LocalDateTime.now();
        }
    }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }



    public boolean isTopLevel() {
        return parentMessage == null;
    }

    public String getFormattedTime() {
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
    }
}
