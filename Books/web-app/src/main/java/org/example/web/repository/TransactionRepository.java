package org.example.web.repository;

import org.example.web.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    @Query("SELECT t FROM Transaction t WHERE t.book = :book AND t.status = :status")
    List<Transaction> findByBookAndStatus(@Param("book") org.example.web.model.Book book,
                                        @Param("status") Transaction.TransactionStatus status);



    @Query("SELECT t FROM Transaction t JOIN FETCH t.book WHERE t.borrower.id = :userId AND t.status = :status")
    List<Transaction> findByBorrowerIdAndStatus(@Param("userId") String userId, @Param("status") Transaction.TransactionStatus status);

    // Find all transactions for a given book (any status)
    @Query("SELECT t FROM Transaction t WHERE t.book = :book")
    List<Transaction> findByBook(@Param("book") org.example.web.model.Book book);

    // Find all transactions where given user is the borrower (any status)
    @Query("SELECT t FROM Transaction t WHERE t.borrower.id = :userId")
    List<Transaction> findByBorrowerId(@Param("userId") String userId);
}
