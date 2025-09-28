package org.example.web.controller;

import org.example.web.dto.BorrowRequest;
import org.example.web.model.Transaction;
import org.example.web.model.User;
import org.example.web.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/transactions")
@CrossOrigin(origins = "*")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;





    @PostMapping("/books/{bookId}/borrow")
    public ResponseEntity<Transaction> borrowBook(@PathVariable String bookId, @RequestBody(required = false) BorrowRequest request) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            // Use current user's ID regardless of request
            Transaction transaction = transactionService.borrowBook(bookId, currentUser.getId());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/books/{bookId}/reserve")
    public ResponseEntity<Transaction> reserveBook(@PathVariable String bookId, @RequestBody(required = false) BorrowRequest request) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            // Use current user's ID regardless of request
            Transaction transaction = transactionService.reserveBook(bookId, currentUser.getId());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/books/{bookId}/return")
    public ResponseEntity<Transaction> returnBook(@PathVariable String bookId) {
        try {
            Transaction transaction = transactionService.returnBook(bookId);
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }





}
