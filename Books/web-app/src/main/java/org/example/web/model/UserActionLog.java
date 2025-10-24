package org.example.web.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_action_logs")
public class UserActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(nullable = false)
    private String userName;

    @Column(nullable = false)
    private String actionType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column
    private String targetId;

    @Column
    private String targetType;

    @Column(nullable = false)
    private LocalDateTime timestamp;



    // Constructors
    public UserActionLog() {
        this.timestamp = LocalDateTime.now();
    }

    public UserActionLog(User user, String actionType, String description) {
        this.userId = user.getId();
        this.userName = user.getFullName();
        this.actionType = actionType;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public UserActionLog(User user, String actionType, String description, String targetId, String targetType) {
        this.userId = user.getId();
        this.userName = user.getFullName();
        this.actionType = actionType;
        this.description = description;
        this.targetId = targetId;
        this.targetType = targetType;
        this.timestamp = LocalDateTime.now();
    }

    public UserActionLog(String userId, String userName, String actionType, String description) {
        this.userId = userId;
        this.userName = userName;
        this.actionType = actionType;
        this.description = description;
        this.timestamp = LocalDateTime.now();
    }

    public UserActionLog(String userId, String userName, String actionType, String description, String targetId, String targetType) {
        this.userId = userId;
        this.userName = userName;
        this.actionType = actionType;
        this.description = description;
        this.targetId = targetId;
        this.targetType = targetType;
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }



    // Helper methods for creating common action types
    public static UserActionLog bookCreated(User user, Book book) {
        return new UserActionLog(user, "BOOK_CREATED",
            "Created book: " + book.getTitle(), book.getId(), "BOOK");
    }

    public static UserActionLog bookBorrowed(User user, Book book) {
        return new UserActionLog(user, "BOOK_BORROWED",
            "Borrowed book: " + book.getTitle(), book.getId(), "BOOK");
    }

    public static UserActionLog bookReturned(User user, Book book) {
        return new UserActionLog(user, "BOOK_RETURNED",
            "Returned book: " + book.getTitle(), book.getId(), "BOOK");
    }

    public static UserActionLog bookReserved(User user, Book book) {
        return new UserActionLog(user, "BOOK_RESERVED",
            "Reserved book: " + book.getTitle(), book.getId(), "BOOK");
    }

    public static UserActionLog messageSent(User user, Message message) {
        return new UserActionLog(user, "MESSAGE_SENT",
            "Sent message about book: " + message.getBook().getTitle(),
            message.getId(), "MESSAGE");
    }

    public static UserActionLog commentAdded(User user, Comment comment) {
        return new UserActionLog(user, "COMMENT_ADDED",
            "Added comment to book: " + comment.getBook().getTitle(),
            comment.getId(), "COMMENT");
    }

    public static UserActionLog bookDeleted(User user, Book book) {
        return new UserActionLog(user, "BOOK_DELETED",
            "Deleted book: " + book.getTitle(), book.getId(), "BOOK");
    }

    public static UserActionLog userDeleted(User adminUser, String deletedUserName, String deletedUserId) {
        return new UserActionLog(adminUser, "USER_DELETED",
            "Deleted user: " + deletedUserName, deletedUserId, "USER");
    }
}
