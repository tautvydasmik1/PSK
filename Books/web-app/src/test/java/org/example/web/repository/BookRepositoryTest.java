package org.example.web.repository;

import org.example.web.model.Book;
import org.example.web.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;

    @Test
    void testInsertBook() {
        Book book = new Book();
        book.setTitle("JUnit Book");
        book.setAuthor("JUnit Author");
        // Optionally set other required fields if needed
        Book saved = bookRepository.save(book);
        assertNotNull(saved.getId());
        assertEquals("JUnit Book", saved.getTitle());
    }
}

