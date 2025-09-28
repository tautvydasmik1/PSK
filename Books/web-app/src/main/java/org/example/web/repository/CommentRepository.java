package org.example.web.repository;

import org.example.web.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, String> {

    // Find all top-level comments for a book (no parent)
    @Query("SELECT c FROM Comment c WHERE c.book.id = :bookId AND c.parentComment IS NULL AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findTopLevelCommentsByBookId(@Param("bookId") String bookId);

    // Find all comments for a book (including replies)
    @Query("SELECT c FROM Comment c WHERE c.book.id = :bookId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findAllCommentsByBookId(@Param("bookId") String bookId);



    // Find replies to a specific comment
    @Query("SELECT c FROM Comment c WHERE c.parentComment.id = :parentCommentId AND c.isDeleted = false ORDER BY c.createdAt ASC")
    List<Comment> findRepliesByParentCommentId(@Param("parentCommentId") String parentCommentId);

    // Find all comments for a book with their replies (nested structure)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.replies r WHERE c.book.id = :bookId AND c.parentComment IS NULL AND c.isDeleted = false AND (r IS NULL OR r.isDeleted = false) ORDER BY c.createdAt ASC, r.createdAt ASC")
    List<Comment> findTopLevelCommentsWithRepliesByBookId(@Param("bookId") String bookId);

    // Count comments for a book
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.book.id = :bookId AND c.isDeleted = false")
    long countCommentsByBookId(@Param("bookId") String bookId);



    // Find comment by ID and ensure it's not deleted
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Comment> findByIdAndNotDeleted(@Param("id") String id);




}
