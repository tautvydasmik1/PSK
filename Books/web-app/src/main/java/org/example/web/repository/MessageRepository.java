package org.example.web.repository;

import org.example.web.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {



    @Query("SELECT m FROM Message m WHERE (m.sender.id = :senderId OR m.book.owner.id = :bookOwnerId) ORDER BY m.createdAt DESC")
    List<Message> findBySenderIdOrBookOwnerIdOrderByCreatedAtDesc(@Param("senderId") String senderId, @Param("bookOwnerId") String bookOwnerId);


}
