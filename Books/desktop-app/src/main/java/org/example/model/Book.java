package org.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {
    private String id;
    private String title;
    private String author;
    private BookCategory category;
    private String description;
    private BookStatus status;
    private String ownerId;
    private String ownerName;
    @JsonProperty("createdAt")
    private LocalDateTime createdAt;


    private Integer publicationYear;

    public enum BookStatus {
        AVAILABLE,
        BORROWED,
        RESERVED
    }

    public enum BookCategory {
        FICTION("Fiction"),
        NON_FICTION("Non-Fiction"),
        SCIENCE_FICTION("Science Fiction"),
        FANTASY("Fantasy"),
        HISTORY("History"),
        BIOGRAPHY("Biography"),
        ROMANCE("Romance"),
        MYSTERY("Mystery"),
        THRILLER("Thriller"),
        ADVENTURE("Adventure");

        private final String displayName;

        BookCategory(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }

        public static BookCategory fromDisplayName(String displayName) {
            for (BookCategory category : values()) {
                if (category.displayName.equalsIgnoreCase(displayName)) {
                    return category;
                }
            }
            throw new IllegalArgumentException("Unknown category: " + displayName);
        }
    }



    // Default constructor for JSON deserialization
    public Book() {
        this.createdAt = LocalDateTime.now();

        this.status = BookStatus.AVAILABLE;
    }

    public Book(String id, String title, String author, BookCategory category, String description,
                BookStatus status, String ownerId, String ownerName) {
        this();
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.description = description;
        this.status = status;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public BookCategory getCategory() { return category; }
    public String getDescription() { return description; }
    public BookStatus getStatus() { return status; }
    public String getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public LocalDateTime getCreatedAt() { return createdAt; }


    public Integer getPublicationYear() { return publicationYear; }


    // Setters for JSON deserialization
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }
    public void setCategory(BookCategory category) { this.category = category; }

    // Helper method for setting category from string (backwards compatibility)
    public void setCategory(String categoryName) {
        if (categoryName != null) {
            try {
                // First try to match by enum name (for JSON deserialization from backend)
                this.category = BookCategory.valueOf(categoryName);
            } catch (IllegalArgumentException e) {
                try {
                    // Then try to match by display name (for UI input)
                    this.category = BookCategory.fromDisplayName(categoryName);
                } catch (IllegalArgumentException e2) {
                    // If both fail, default to FICTION
                    this.category = BookCategory.FICTION;
                }
            }
        }
    }
    public void setDescription(String description) { this.description = description; }
    public void setStatus(BookStatus status) { this.status = status; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }


    @Override
    public String toString() {
        return title + " by " + author + " (" + status + ")";
    }
}
