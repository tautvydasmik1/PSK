package org.example.web.service;

import org.example.web.model.Book;
import org.example.web.model.Transaction;
import org.example.web.model.User;
import org.example.web.repository.BookRepository;
import org.example.web.repository.TransactionRepository;
import org.example.web.repository.UserRepository;
import org.example.web.model.UserActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActionLogService userActionLogService;





    public Transaction borrowBook(String bookId, String userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Allow borrowing from both AVAILABLE and RESERVED status
        if (book.getStatus() != Book.BookStatus.AVAILABLE &&
            book.getStatus() != Book.BookStatus.RESERVED) {
            throw new RuntimeException("Book is not available for borrowing (current status: " + book.getStatus() + ")");
        }

        // If book is RESERVED, only the person who reserved it can borrow it
        if (book.getStatus() == Book.BookStatus.RESERVED) {
            List<Transaction> activeReservations = transactionRepository.findByBookAndStatus(
                book, Transaction.TransactionStatus.ACTIVE);

            if (!activeReservations.isEmpty()) {
                Transaction reservation = activeReservations.get(0);
                if (!reservation.getBorrower().getId().equals(userId)) {
                    throw new RuntimeException("This book is reserved by another user");
                }
            }
        }

        // Get the borrower user
        User borrower = null;
        if (userId != null) {
            borrower = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            borrower = getOrCreateDefaultUser();
        }

        User lender = book.getOwner();

        // If book was reserved, complete the reservation transaction first
        if (book.getStatus() == Book.BookStatus.RESERVED) {
            List<Transaction> activeReservations = transactionRepository.findByBookAndStatus(
                book, Transaction.TransactionStatus.ACTIVE);

            for (Transaction reservation : activeReservations) {
                if (reservation.getType() == Transaction.TransactionType.RESERVE) {
                    reservation.setStatus(Transaction.TransactionStatus.COMPLETED);
                    transactionRepository.save(reservation);
                }
            }
        }

        // Create new borrow transaction
        Transaction transaction = new Transaction(book, borrower, lender, Transaction.TransactionType.BORROW);
        transaction.setStatus(Transaction.TransactionStatus.ACTIVE);

        // Update book status
        book.setStatus(Book.BookStatus.BORROWED);
        bookRepository.save(book);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the book borrow action
        userActionLogService.logAction(UserActionLog.bookBorrowed(borrower, book));

        return savedTransaction;
    }

    public Transaction reserveBook(String bookId, String userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        if (book.getStatus() != Book.BookStatus.AVAILABLE) {
            throw new RuntimeException("Book is not available for reservation");
        }

        // Get the borrower user
        User borrower = null;
        if (userId != null) {
            borrower = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            borrower = getOrCreateDefaultUser();
        }

        User lender = book.getOwner();

        // Create transaction
        Transaction transaction = new Transaction(book, borrower, lender, Transaction.TransactionType.RESERVE);
        transaction.setStatus(Transaction.TransactionStatus.ACTIVE);

        // Update book status
        book.setStatus(Book.BookStatus.RESERVED);
        bookRepository.save(book);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the book reserve action
        userActionLogService.logAction(UserActionLog.bookReserved(borrower, book));

        return savedTransaction;
    }

    public Transaction returnBook(String bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        // Find the active transaction for this book
        List<Transaction> activeTransactions = transactionRepository.findByBookAndStatus(
                book, Transaction.TransactionStatus.ACTIVE);

        if (activeTransactions.isEmpty()) {
            throw new RuntimeException("No active transaction found for this book");
        }

        Transaction transaction = activeTransactions.get(0);
        transaction.setStatus(Transaction.TransactionStatus.COMPLETED);

        // Update book status back to available
        book.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.save(book);

        Transaction savedTransaction = transactionRepository.save(transaction);

        // Log the book return action
        userActionLogService.logAction(UserActionLog.bookReturned(transaction.getBorrower(), book));

        return savedTransaction;
    }





    // Helper method to get or create a default user for demo purposes
    private User getOrCreateDefaultUser() {
        // First try to find existing demo user
        User existingUser = userRepository.findByUsername("demo-user").orElse(null);
        if (existingUser != null) {
            return existingUser;
        }

        // If not found, create a new one
        User defaultUser = new User(
            "demo-user",
            "password",
            "Demo",
            "User",
            "demo@example.com",
            "123-456-7890",
            java.time.LocalDate.of(1990, 1, 1),
            User.UserType.REGULAR_USER
        );
        return userRepository.save(defaultUser);
    }
}
