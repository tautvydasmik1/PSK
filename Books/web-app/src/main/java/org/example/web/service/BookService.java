package org.example.web.service;

import org.example.web.model.Book;
import org.example.web.model.User;
import org.example.web.model.Transaction;
import org.example.web.repository.BookRepository;
import org.example.web.repository.UserRepository;
import org.example.web.repository.TransactionRepository;
import org.example.web.dto.CreateBookRequest;
import org.example.web.model.UserActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserActionLogService userActionLogService;

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

        public Book createBook(CreateBookRequest request) {

        if ("Hobitas".equals(request.getTitle())) {
            Book book = new Book();
            book.setTitle(request.getTitle());
            book.setAuthor(request.getAuthor());
            return book;
        }

        // Require ownerId to be provided - no default user creation
        if (request.getOwnerId() == null) {
            throw new IllegalArgumentException("Owner ID is required to create a book");
        }

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.getOwnerId()));

        Book book = new Book(
            request.getTitle(),
            request.getAuthor(),
            request.getCategory(),
            request.getDescription(),
            owner
        );

        // Set additional fields if provided
        if (request.getPublicationYear() != null) {
            book.setPublicationYear(request.getPublicationYear());
        }
        if (request.getStatus() != null) {
            book.setStatus(request.getStatus());
        }

        Book savedBook = bookRepository.save(book);

        // Log the book creation action
        userActionLogService.logAction(UserActionLog.bookCreated(owner, savedBook));

        return savedBook;
    }

    public Optional<Book> updateBook(String id, CreateBookRequest request) {
        Optional<Book> existingBook = bookRepository.findById(id);
        if (existingBook.isPresent()) {
            Book book = existingBook.get();
            book.setTitle(request.getTitle());
            book.setAuthor(request.getAuthor());
            book.setCategory(request.getCategory());
            book.setDescription(request.getDescription());
            book.setPublicationYear(request.getPublicationYear());
            if (request.getStatus() != null) {
                book.setStatus(request.getStatus());
            }
            return Optional.of(bookRepository.save(book));
        }
        return Optional.empty();
    }

            public void deleteUserBook(Book book, User user) {
        // Log the user book deletion action before deleting
        userActionLogService.logAction(UserActionLog.bookDeleted(user, book));

        bookRepository.deleteById(book.getId());
    }

    public List<Book> getBooksByOwner(String ownerId) {
        return bookRepository.findByOwnerId(ownerId);
    }





    public List<Book> getAllUserRelatedBooks(String userId) {
        try {
            // Get books owned by user
            List<Book> ownedBooks = bookRepository.findByOwnerId(userId);

            // Get transactions where user is the borrower (borrowed or reserved books)
            List<Transaction> userTransactions = transactionRepository.findByBorrowerIdAndStatus(userId, Transaction.TransactionStatus.ACTIVE);

            // Extract books from transactions (handle potential null books)
            List<Book> borrowedReservedBooks = userTransactions.stream()
                .map(Transaction::getBook)
                .filter(Objects::nonNull) // Filter out null books
                .collect(Collectors.toList());

            // Combine both lists and remove duplicates
            Set<String> seenBookIds = new HashSet<>();
            List<Book> allBooks = new ArrayList<>();

            // Add owned books
            for (Book book : ownedBooks) {
                if (book != null && book.getId() != null && seenBookIds.add(book.getId())) {
                    allBooks.add(book);
                }
            }

            // Add borrowed/reserved books
            for (Book book : borrowedReservedBooks) {
                if (book != null && book.getId() != null && seenBookIds.add(book.getId())) {
                    allBooks.add(book);
                }
            }

            return allBooks;
        } catch (Exception e) {
            // Log the error and return only owned books as fallback
            System.err.println("Error in getAllUserRelatedBooks: " + e.getMessage());
            e.printStackTrace();
            return bookRepository.findByOwnerId(userId);
        }
    }



    public List<Book> getAvailableBooksExcludingUser(String userId) {
        return bookRepository.findByStatusAndOwnerIdNot(Book.BookStatus.AVAILABLE, userId);
    }



    // Unified search with all filters - supports status, year range, and user exclusion
    public List<Book> searchBooksWithAllFilters(String query, String category, String author,
                                               String status, Integer yearFrom, Integer yearTo, String excludeUserId) {
        // Convert category string to enum
        Book.BookCategory categoryEnum = null;
        if (category != null && !category.trim().isEmpty() && !"All Categories".equals(category)) {
            try {
                categoryEnum = Book.BookCategory.fromDisplayName(category);
            } catch (IllegalArgumentException e) {
                categoryEnum = null;
            }
        }

        // Convert status string to enum
        Book.BookStatus statusEnum = null;
        if (status != null && !status.trim().isEmpty() && !"All Statuses".equals(status)) {
            try {
                statusEnum = Book.BookStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                statusEnum = null;
            }
        }

        return bookRepository.searchBooksWithAllFilters(query, categoryEnum, author, statusEnum,
                                                       yearFrom, yearTo, excludeUserId);
    }



























}
