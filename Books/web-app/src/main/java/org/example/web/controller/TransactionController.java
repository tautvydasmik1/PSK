package org.example.web.controller;

import org.example.web.dto.BorrowRequest;
import org.example.web.model.Transaction;
import org.example.web.model.User;
import org.example.web.service.AuthenticatedUserService;
import org.example.web.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;

    @PostMapping("/books/{bookId}/borrow")
    public ResponseEntity<Transaction> borrowBook(@PathVariable String bookId, @RequestBody(required = false) BorrowRequest request) {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            Transaction transaction = transactionService.borrowBook(bookId, currentUser.getId());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/books/{bookId}/reserve")
    public ResponseEntity<Transaction> reserveBook(@PathVariable String bookId, @RequestBody(required = false) BorrowRequest request) {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            Transaction transaction = transactionService.reserveBook(bookId, currentUser.getId());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/books/{bookId}/return")
    public ResponseEntity<Transaction> returnBook(@PathVariable String bookId) {
        try {
            Transaction transaction = transactionService.returnBook(bookId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }



}
