package org.example.web.controller;

import org.example.web.model.Book;
import org.example.web.model.User;
import org.example.web.service.AuthenticatedUserService;
import org.example.web.service.BookService;
import org.example.web.dto.CreateBookRequest;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;
import java.util.Arrays;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/books")
@CrossOrigin(origins = "*")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = bookService.getAllBooks();
        return ResponseEntity.ok(books);
    }


    @GetMapping("/search")
    public ResponseEntity<List<Book>> searchBooks(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer yearFrom,
            @RequestParam(required = false) Integer yearTo,
            @RequestParam(required = false, defaultValue = "true") Boolean excludeCurrentUser) {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            // Exclude current user's books by default (for browse functionality)
            String excludeUserId = excludeCurrentUser ? currentUser.getId() : null;

            List<Book> books = bookService.searchBooksWithAllFilters(
                query, category, author, status, yearFrom, yearTo, excludeUserId);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    // Allow any authenticated user to create a book (not admin-only)
    @PostMapping
    public ResponseEntity<Book> createBook(@Valid @RequestBody CreateBookRequest request) {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            // Set the owner to the current authenticated user
            request.setOwnerId(currentUser.getId());

            Book newBook = bookService.createBook(request);
            return ResponseEntity.status(201).body(newBook); // <-- 201 Created
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Book> updateBook(@PathVariable String id, @Valid @RequestBody CreateBookRequest request) {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            // Check if user owns the book
            Optional<Book> existingBook = bookService.getBookById(id);
            if (existingBook.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Book book = existingBook.get();
            if (!book.getOwner().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }

            Optional<Book> updatedBook = bookService.updateBook(id, request);
            return updatedBook.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable String id) {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            // Check if user owns the book
            Optional<Book> existingBook = bookService.getBookById(id);
            if (existingBook.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Book book = existingBook.get();
            if (!book.getOwner().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(403).build();
            }

            bookService.deleteUserBook(book, currentUser);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all books related to a user (owned + borrowed + reserved)
    @GetMapping("/user/{userId}/all-related")
    public ResponseEntity<List<Book>> getAllUserRelatedBooks(@PathVariable String userId) {
        try {
            List<Book> books = bookService.getAllUserRelatedBooks(userId);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get available books excluding current user's books
    @GetMapping("/available/exclude-current-user")
    public ResponseEntity<List<Book>> getAvailableBooksExcludingCurrentUser() {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            List<Book> books = bookService.getAvailableBooksExcludingUser(currentUser.getId());
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getBookCategories() {
        // Return all available categories as display names
        List<String> categories = Arrays.stream(Book.BookCategory.values())
                .map(Book.BookCategory::getDisplayName)
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }
}
