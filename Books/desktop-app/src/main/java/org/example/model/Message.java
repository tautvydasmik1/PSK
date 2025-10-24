package org.example.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Message {
    private String id;
    private String bookId;
    private String bookTitle;
    private String senderId;
    private String senderName;
    private String recipientId;
    private String recipientName;

    private String content;
    private LocalDateTime createdAt;
    private boolean isRead;
    private String parentMessageId; // For nested comments
    private List<Message> replies;
    private int depth; // For unlimited depth comments
    private boolean isDeleted;
    private LocalDateTime deletedAt;


    // Default constructor for JSON deserialization
    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.replies = new ArrayList<>();
        this.depth = 0;
        this.isDeleted = false;

    }

    public Message(String id, String bookId, String senderId, String senderName,
                  String content, String parentMessageId) {
        this.id = id;
        this.bookId = bookId;
        this.senderId = senderId;
        this.senderName = senderName;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.parentMessageId = parentMessageId;
        this.replies = new ArrayList<>();
        this.depth = parentMessageId == null ? 0 : 1; // Will be calculated properly
        this.isDeleted = false;

    }

    // Getters
    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public String getBookTitle() { return bookTitle; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getRecipientId() { return recipientId; }
    public String getRecipientName() { return recipientName; }

    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isRead() { return isRead; }
    public String getParentMessageId() { return parentMessageId; }
    public List<Message> getReplies() { return replies; }
    public int getDepth() { return depth; }
    public boolean isDeleted() { return isDeleted; }
    public LocalDateTime getDeletedAt() { return deletedAt; }


    // Setters
    public void setId(String id) { this.id = id; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public void setContent(String content) { this.content = content; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRead(boolean read) { this.isRead = read; }
    public void setParentMessageId(String parentMessageId) { this.parentMessageId = parentMessageId; }
    public void setReplies(List<Message> replies) { this.replies = replies; }
    public void setDepth(int depth) { this.depth = depth; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }


    public void addReply(Message reply) {
        this.replies.add(reply);
        reply.setDepth(this.depth + 1);
    }

    public boolean isTopLevel() {
        return parentMessageId == null;
    }

    public String getFormattedTime() {
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, HH:mm"));
    }

    public String getFormattedCreatedAt() {
        return createdAt.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm"));
    }

}
