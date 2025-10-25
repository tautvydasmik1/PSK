// filepath: c:\Users\luksv\Downloads\PSK-main\Books\web-app\src\main\java\org\example\web\repository\MessageRepository.java
package org.example.web.repository;

import org.example.web.model.Message;
import org.example.web.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {

    @Query("SELECT m FROM Message m WHERE m.book = :book")
    List<Message> findByBook(@Param("book") Book book);

    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.recipient.id = :userId")
    List<Message> findBySenderOrRecipient(@Param("userId") String userId);

    // Messages where the user is sender or the owner of the related book, ordered newest first
    @Query("SELECT m FROM Message m WHERE m.sender.id = :userId OR m.book.owner.id = :userId ORDER BY m.createdAt DESC")
    List<Message> findMessagesForUser(@Param("userId") String userId);
}
