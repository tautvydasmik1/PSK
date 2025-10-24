package org.example.web.dto;

import org.example.web.model.Book;

public class CreateBookRequest {
    private String title;
    private String author;
    private Book.BookCategory category;
    private String description;
    private String ownerId; // For now, we'll use a simple owner ID

    private Integer publicationYear;
    private Book.BookStatus status;

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public Book.BookCategory getCategory() { return category; }
    public void setCategory(Book.BookCategory category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }



    public Integer getPublicationYear() { return publicationYear; }
    public void setPublicationYear(Integer publicationYear) { this.publicationYear = publicationYear; }



    public Book.BookStatus getStatus() { return status; }
    public void setStatus(Book.BookStatus status) { this.status = status; }
}
