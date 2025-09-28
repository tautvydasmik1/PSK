package org.example.web.service;

import org.example.web.model.Book;
import org.example.web.model.Comment;
import org.example.web.model.User;
import org.example.web.repository.BookRepository;
import org.example.web.repository.CommentRepository;
import org.example.web.repository.UserRepository;
import org.example.web.model.UserActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserActionLogService userActionLogService;

    // Create a new top-level comment
    public Comment createComment(String content, String authorId, String bookId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + authorId));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + bookId));

        Comment comment = new Comment(content, author, book);
        Comment savedComment = commentRepository.save(comment);

        // Log the comment creation action
        userActionLogService.logAction(UserActionLog.commentAdded(author, savedComment));

        return savedComment;
    }

    // Create a reply to an existing comment
    public Comment createReply(String content, String authorId, String parentCommentId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + authorId));

        Comment parentComment = commentRepository.findByIdAndNotDeleted(parentCommentId)
                .orElseThrow(() -> new IllegalArgumentException("Parent comment not found with ID: " + parentCommentId));

        Comment reply = new Comment(content, author, parentComment.getBook(), parentComment);
        Comment savedReply = commentRepository.save(reply);

        // Add the reply to the parent comment's replies list
        parentComment.addReply(savedReply);
        commentRepository.save(parentComment);

        // Log the comment reply action
        userActionLogService.logAction(UserActionLog.commentAdded(author, savedReply));

        return savedReply;
    }

    // Get all top-level comments for a book


    // Get all comments for a book (including replies)


    // Get top-level comments with their replies (nested structure)
    public List<Comment> getTopLevelCommentsWithRepliesByBookId(String bookId) {
        return commentRepository.findTopLevelCommentsWithRepliesByBookId(bookId);
    }

    // Get replies to a specific comment






    // Update a comment
    public Comment updateComment(String commentId, String newContent, String authorId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

        // Check if the user is the author of the comment
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("User is not authorized to update this comment");
        }

        comment.setContent(newContent);
        return commentRepository.save(comment);
    }

    // Soft delete a comment (mark as deleted)
    public boolean deleteComment(String commentId, String authorId) {
        Comment comment = commentRepository.findByIdAndNotDeleted(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Comment not found with ID: " + commentId));

        // Check if the user is the author of the comment
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new IllegalArgumentException("User is not authorized to delete this comment");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
        return true;
    }

    // Get comment count for a book


    // Get top-level comment count for a book


    // Check if a comment exists and is not deleted
    public boolean commentExists(String commentId) {
        return commentRepository.findByIdAndNotDeleted(commentId).isPresent();
    }



    // Get all comments in a flat list (for easier processing)
    public List<Comment> getCommentsFlatList(String bookId) {
        return commentRepository.findAllCommentsByBookId(bookId);
    }
}
