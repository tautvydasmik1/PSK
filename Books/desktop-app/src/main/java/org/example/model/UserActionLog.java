package org.example.model;

import java.time.LocalDateTime;

public class UserActionLog {
    private String id;
    private String userId;
    private String userName;
    private String actionType;
    private String description;
    private String targetId;
    private String targetType;
    private LocalDateTime timestamp;


    public UserActionLog() {}

    public UserActionLog(String userId, String userName, String actionType, String description,
                       String targetId, String targetType) {
        this.userId = userId;
        this.userName = userName;
        this.actionType = actionType;
        this.description = description;
        this.targetId = targetId;
        this.targetType = targetType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTargetId() { return targetId; }
    public void setTargetId(String targetId) { this.targetId = targetId; }

    public String getTargetType() { return targetType; }
    public void setTargetType(String targetType) { this.targetType = targetType; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }


}
