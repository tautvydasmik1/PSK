package org.example.web.service;

import org.example.web.model.Book;
import org.example.web.model.Message;
import org.example.web.model.User;
import org.example.web.repository.BookRepository;
import org.example.web.repository.MessageRepository;
import org.example.web.repository.UserRepository;
import org.example.web.model.UserActionLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserActionLogService userActionLogService;





    public List<Message> getUserMessages(String userId) {
        return messageRepository.findBySenderIdOrBookOwnerIdOrderByCreatedAtDesc(userId, userId);
    }



    public boolean deleteMessage(String messageId, String userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        // Only allow deletion by sender or recipient
        if (!message.getSender().getId().equals(userId) &&
            (message.getRecipient() == null || !message.getRecipient().getId().equals(userId))) {
            throw new RuntimeException("User not authorized to delete this message");
        }

        // Soft delete - mark as deleted but keep in database
        message.setDeleted(true);
        messageRepository.save(message);
        return true;
    }



    // New methods for the updated messaging system
    public Message sendMessage(String senderId, String bookId, String content, String parentMessageId, String recipientId) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        Message parentMessage = null;
        if (parentMessageId != null && !parentMessageId.isEmpty()) {
            parentMessage = messageRepository.findById(parentMessageId)
                    .orElseThrow(() -> new RuntimeException("Parent message not found"));
        }

        Message message = new Message(book, sender, content, parentMessage);

        // If recipientId is provided, use it
        if (recipientId != null && !recipientId.isEmpty()) {
            User recipient = userRepository.findById(recipientId)
                    .orElseThrow(() -> new RuntimeException("Recipient not found"));
            message.setRecipient(recipient);
        }
        // If no recipientId provided, set book owner as recipient (unless sender is the book owner)
        else if (!sender.getId().equals(book.getOwner().getId())) {
            message.setRecipient(book.getOwner());
        }
        // If sender is the book owner and no recipient specified, this is a general message to the book
        else {
            // For now, we'll set the book owner as recipient even if they're the sender
            // This allows for general book-related messages
            message.setRecipient(book.getOwner());
        }

        Message savedMessage = messageRepository.save(message);

        // Log the message sent action
        userActionLogService.logAction(UserActionLog.messageSent(sender, savedMessage));

        return savedMessage;
    }












}
