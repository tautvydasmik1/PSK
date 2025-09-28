package org.example.web.controller;

import org.example.web.dto.CreateCommentRequest;
import org.example.web.dto.UpdateCommentRequest;
import org.example.web.model.Comment;
import org.example.web.model.User;
import org.example.web.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/comments")
@CrossOrigin(origins = "*")
public class CommentController {

    @Autowired
    private CommentService commentService;

    // Create a new top-level comment
    @PostMapping("/books/{bookId}")
    public ResponseEntity<Comment> createComment(@PathVariable String bookId, @RequestBody CreateCommentRequest request) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            Comment comment = commentService.createComment(request.getContent(), currentUser.getId(), bookId);
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Create a reply to an existing comment
    @PostMapping("/{commentId}/replies")
    public ResponseEntity<Comment> createReply(@PathVariable String commentId, @RequestBody CreateCommentRequest request) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            Comment reply = commentService.createReply(request.getContent(), currentUser.getId(), commentId);
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get all top-level comments for a book


    // Get all comments for a book (including replies)


    // Get top-level comments with their replies (nested structure)
    @GetMapping("/books/{bookId}/nested")
    public ResponseEntity<List<Comment>> getTopLevelCommentsWithRepliesByBookId(@PathVariable String bookId) {
        try {
            List<Comment> comments = commentService.getTopLevelCommentsWithRepliesByBookId(bookId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get replies to a specific comment








    // Update a comment
    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(@PathVariable String commentId, @RequestBody UpdateCommentRequest request) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            Comment updatedComment = commentService.updateComment(commentId, request.getContent(), currentUser.getId());
            return ResponseEntity.ok(updatedComment);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete a comment (soft delete)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable String commentId) {
        try {
            // Get current authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = (User) auth.getPrincipal();

            boolean deleted = commentService.deleteComment(commentId, currentUser.getId());
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(403).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Get comment count for a book










}
