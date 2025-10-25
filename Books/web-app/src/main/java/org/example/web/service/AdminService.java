package org.example.web.service;

import org.example.web.model.Book;
import org.example.web.model.User;
import org.example.web.model.UserActionLog;
import org.example.web.repository.BookRepository;
import org.example.web.repository.UserRepository;
import org.example.web.repository.TransactionRepository;
import org.example.web.repository.MessageRepository;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserActionLogService userActionLogService;


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    @Transactional
    public boolean deleteUser(String userId) {
        if (!userRepository.existsById(userId)) {
            return false;
        }

        // Delete transactions where user is borrower
        var borrowerTxs = transactionRepository.findByBorrowerId(userId);
        if (borrowerTxs != null && !borrowerTxs.isEmpty()) {
            transactionRepository.deleteAll(borrowerTxs);
        }

        // Delete messages where user is sender or recipient
        var userMessages = messageRepository.findBySenderOrRecipient(userId);
        if (userMessages != null && !userMessages.isEmpty()) {
            messageRepository.deleteAll(userMessages);
        }

        // Delete books owned by user and any transactions associated with those books
        List<Book> owned = bookRepository.findByOwnerId(userId);
        for (Book b : owned) {
            var txs = transactionRepository.findByBook(b);
            if (txs != null && !txs.isEmpty()) {
                transactionRepository.deleteAll(txs);
            }
            // Delete messages for this book
            var msgs = messageRepository.findByBook(b);
            if (msgs != null && !msgs.isEmpty()) {
                messageRepository.deleteAll(msgs);
            }
            bookRepository.deleteById(b.getId());
            // Optionally log book deletion per owner - skipped here
        }

        // Finally delete the user
        userRepository.deleteById(userId);
        return true;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Transactional
    public boolean deleteBook(String bookId) {
        if (!bookRepository.existsById(bookId)) {
            return false;
        }

        // Remove transactions referring to the book first to avoid FK constraint
        var bookOpt = bookRepository.findById(bookId);
        if (bookOpt.isPresent()) {
            Book b = bookOpt.get();
            var txs = transactionRepository.findByBook(b);
            if (txs != null && !txs.isEmpty()) {
                transactionRepository.deleteAll(txs);
            }
            // Remove messages referring to the book (FK constraint)
            var msgs = messageRepository.findByBook(b);
            if (msgs != null && !msgs.isEmpty()) {
                messageRepository.deleteAll(msgs);
            }
        }

        bookRepository.deleteById(bookId);
        return true;
    }

    public List<UserActionLog> getAllUserActions() {
        // Use the efficient database-based approach
        return userActionLogService.getAllActions();
    }

    public List<UserActionLog> getActionsByType(String actionType) {
        // Use the efficient database-based approach
        return userActionLogService.getActionsByType(actionType);
    }


}
