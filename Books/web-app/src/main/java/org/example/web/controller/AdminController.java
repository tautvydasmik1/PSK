package org.example.web.controller;

import org.example.web.model.Book;
import org.example.web.model.User;
import org.example.web.model.UserActionLog;
import org.example.web.service.AdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;



    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = adminService.getAllUsers();
        return ResponseEntity.ok(users);
    }




    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable String userId) {
        boolean deleted = adminService.deleteUser(userId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/books")
    public ResponseEntity<List<Book>> getAllBooks() {
        List<Book> books = adminService.getAllBooks();
        return ResponseEntity.ok(books);
    }

        @DeleteMapping("/books/{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable String bookId) {
        boolean deleted = adminService.deleteBook(bookId);
        return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }







    // Comprehensive admin dashboard endpoints for requirement #4
    @GetMapping("/user-actions")
    public ResponseEntity<List<UserActionLog>> getAllUserActions() {
        try {
            List<UserActionLog> actions = adminService.getAllUserActions();
            return ResponseEntity.ok(actions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }




    @GetMapping("/actions-by-type")
    public ResponseEntity<List<UserActionLog>> getActionsByType(@RequestParam String actionType) {
        try {
            List<UserActionLog> actions = adminService.getActionsByType(actionType);
            return ResponseEntity.ok(actions);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }











}
