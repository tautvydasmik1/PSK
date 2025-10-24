package org.example.web.dto;

import org.example.web.model.Message;
import java.time.LocalDateTime;

public class MessageDto {
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
    private String parentMessageId;
    private int depth;
    private boolean isDeleted;
    private LocalDateTime deletedAt;


    // Default constructor
    public MessageDto() {}

    // Constructor from Message entity
    public MessageDto(Message message) {
        this.id = message.getId();
        this.bookId = message.getBookId();
        this.bookTitle = message.getBook() != null ? message.getBook().getTitle() : null;
        this.senderId = message.getSenderId();
        this.senderName = message.getSender() != null ?
            message.getSender().getFirstName() + " " + message.getSender().getLastName() : null;
        this.recipientId = message.getRecipientId();
        this.recipientName = message.getRecipient() != null ?
            message.getRecipient().getFirstName() + " " + message.getRecipient().getLastName() : null;

        this.content = message.getContent();
        this.createdAt = message.getCreatedAt();
        this.isRead = message.isRead();
        this.parentMessageId = message.getParentMessageId();
        this.depth = message.getDepth();
        this.isDeleted = message.isDeleted();
        this.deletedAt = message.getDeletedAt();

    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }



    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getParentMessageId() { return parentMessageId; }
    public void setParentMessageId(String parentMessageId) { this.parentMessageId = parentMessageId; }

    public int getDepth() { return depth; }
    public void setDepth(int depth) { this.depth = depth; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { this.isDeleted = deleted; }

    public LocalDateTime getDeletedAt() { return deletedAt; }
    public void setDeletedAt(LocalDateTime deletedAt) { this.deletedAt = deletedAt; }


}
