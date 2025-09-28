package org.example.model;

import java.time.LocalDateTime;

public class Transaction {
    private String id;
    private String bookId;
    private String borrowerId;
    private String lenderId;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public enum TransactionType {
        BORROW,
        RESERVE,
        SALE
    }

    public enum TransactionStatus {
        PENDING,
        ACTIVE,
        COMPLETED,
        CANCELLED
    }

    public Transaction(String id, String bookId, String borrowerId, String lenderId,
                      TransactionType type) {
        this.id = id;
        this.bookId = bookId;
        this.borrowerId = borrowerId;
        this.lenderId = lenderId;
        this.type = type;
        this.status = TransactionStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getBookId() { return bookId; }
    public String getBorrowerId() { return borrowerId; }
    public String getLenderId() { return lenderId; }
    public TransactionType getType() { return type; }
    public TransactionStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCompletedAt() { return completedAt; }

    // Setters
    public void setStatus(TransactionStatus status) { this.status = status; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public boolean isActive() {
        return status == TransactionStatus.ACTIVE;
    }

    public boolean isCompleted() {
        return status == TransactionStatus.COMPLETED;
    }
}
