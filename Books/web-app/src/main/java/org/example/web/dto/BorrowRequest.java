package org.example.web.dto;

/**
 * DTO for book borrowing requests
 * Note: Currently optional as user ID is derived from authentication context
 */
public class BorrowRequest {
    private String userId;

    public BorrowRequest() {}

    public BorrowRequest(String userId) {
        this.userId = userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
