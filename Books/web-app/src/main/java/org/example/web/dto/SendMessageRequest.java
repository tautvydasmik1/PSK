package org.example.web.dto;

public class SendMessageRequest {
    private String bookId;
    private String content;
    private String parentMessageId;
    private String recipientId;

    // Default constructor
    public SendMessageRequest() {}

    public SendMessageRequest(String bookId, String content, String parentMessageId) {
        this.bookId = bookId;
        this.content = content;
        this.parentMessageId = parentMessageId;
    }

    public SendMessageRequest(String bookId, String content, String parentMessageId, String recipientId) {
        this.bookId = bookId;
        this.content = content;
        this.parentMessageId = parentMessageId;
        this.recipientId = recipientId;
    }

    // Getters and Setters
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getParentMessageId() { return parentMessageId; }
    public void setParentMessageId(String parentMessageId) { this.parentMessageId = parentMessageId; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }
}
