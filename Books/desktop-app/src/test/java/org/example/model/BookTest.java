package org.example.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class BookTest {
    @Test
    void testBookFieldsNotNull() {
        Book book = new Book();
        book.setId("1");
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setCategory(Book.BookCategory.FICTION);
        book.setDescription("A test book");
        book.setStatus(Book.BookStatus.AVAILABLE);
        book.setOwnerId("owner1");
        book.setOwnerName("Owner Name");
        book.setCreatedAt(LocalDateTime.now());
        book.setPublicationYear(2020);

        assertNotNull(book.getId());
        assertNotNull(book.getTitle());
        assertNotNull(book.getAuthor());
        assertNotNull(book.getCategory());
        assertNotNull(book.getStatus());
        assertNotNull(book.getOwnerId());
        assertNotNull(book.getOwnerName());
        assertNotNull(book.getCreatedAt());
        assertNotNull(book.getPublicationYear());
    }

    @Test
    void testPublicationYearRange() {
        Book book = new Book();
        book.setPublicationYear(2025);
        assertTrue(book.getPublicationYear() >= 1450 && book.getPublicationYear() <= 2100,
            "Publication year should be between 1450 and 2100");
    }

    @Test
    void testValidCategoryAndStatus() {
        Book book = new Book();
        book.setCategory(Book.BookCategory.FICTION);
        book.setStatus(Book.BookStatus.AVAILABLE);
        assertEquals("Fiction", book.getCategory().getDisplayName());
        assertEquals(Book.BookStatus.AVAILABLE, book.getStatus());
    }
}

