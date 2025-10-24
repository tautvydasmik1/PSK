package org.example.web.repository;

import org.example.web.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {



    // Unified search with all filters including status and year range
    @Query("SELECT b FROM Book b WHERE " +
           "(:query IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:category IS NULL OR b.category = :category) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:yearFrom IS NULL OR b.publicationYear IS NULL OR b.publicationYear >= :yearFrom) AND " +
           "(:yearTo IS NULL OR b.publicationYear IS NULL OR b.publicationYear <= :yearTo) AND " +
           "(:excludeUserId IS NULL OR b.owner.id != :excludeUserId)")
    List<Book> searchBooksWithAllFilters(@Param("query") String query,
                                        @Param("category") Book.BookCategory category,
                                        @Param("author") String author,
                                        @Param("status") Book.BookStatus status,
                                        @Param("yearFrom") Integer yearFrom,
                                        @Param("yearTo") Integer yearTo,
                                        @Param("excludeUserId") String excludeUserId);

    @Query("SELECT b FROM Book b WHERE b.owner.id = :ownerId")
    List<Book> findByOwnerId(@Param("ownerId") String ownerId);

    @Query("SELECT b FROM Book b WHERE b.status = :status AND b.owner.id != :ownerId")
    List<Book> findByStatusAndOwnerIdNot(@Param("status") Book.BookStatus status, @Param("ownerId") String ownerId);

    List<Book> findByStatus(Book.BookStatus status);










}
