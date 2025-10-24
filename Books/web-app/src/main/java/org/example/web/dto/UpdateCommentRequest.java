package org.example.web.dto;

/**
 * DTO for updating existing comments
 */
public class UpdateCommentRequest {
    private String content;

    public UpdateCommentRequest() {}

    public UpdateCommentRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
