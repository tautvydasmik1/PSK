package org.example.web.service;

import org.example.web.model.Book;
import org.example.web.model.User;
import org.example.web.model.UserActionLog;
import org.example.web.repository.BookRepository;
import org.example.web.repository.UserRepository;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserActionLogService userActionLogService;



    public List<User> getAllUsers() {
        return userRepository.findAll();
    }



    public boolean deleteUser(String userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

        public boolean deleteBook(String bookId) {
        if (bookRepository.existsById(bookId)) {
            bookRepository.deleteById(bookId);
            return true;
        }
        return false;
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
