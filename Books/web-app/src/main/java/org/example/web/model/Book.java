package org.example.web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.LocalDateTime;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @Enumerated(EnumType.STRING)
    private BookCategory category;

    private String description;

    @Enumerated(EnumType.STRING)
    private BookStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    @JsonIgnore
    private User owner;

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



    // Default constructor
    public Book() {
        this.createdAt = LocalDateTime.now();

        this.status = BookStatus.AVAILABLE;
    }

    public Book(String title, String author, BookCategory category, String description, User owner) {
        this();
        this.title = title;
        this.author = author;
        this.category = category;
        this.description = description;
        this.owner = owner;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public BookCategory getCategory() { return category; }
    public void setCategory(BookCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BookStatus getStatus() { return status; }
    public void setStatus(BookStatus status) { this.status = status; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getOwnerId() {
        return owner != null ? owner.getId() : null;
    }

    public String getOwnerName() {
        return owner != null ? owner.getFullName() : null;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }





    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }



    @Override
    public String toString() {
        return title + " by " + author + " (" + status + ")";
    }
}
