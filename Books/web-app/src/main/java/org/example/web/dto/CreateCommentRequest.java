package org.example.web.dto;

/**
 * DTO for creating new comments
 */
public class CreateCommentRequest {
    private String content;

    public CreateCommentRequest() {}

    public CreateCommentRequest(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
