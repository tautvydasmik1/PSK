package org.example.web.controller;

import org.example.web.model.Message;
import org.example.web.model.User;
import org.example.web.service.MessageService;
import org.example.web.service.AuthenticatedUserService;
import org.example.web.dto.MessageDto;
import org.example.web.dto.SendMessageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;




@RestController
@RequestMapping("/messages")
@CrossOrigin(origins = "*")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private AuthenticatedUserService authenticatedUserService;


    // Get messages for a specific user (frontend expects this endpoint)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<MessageDto>> getUserMessages(@PathVariable String userId) {
        try {
            List<Message> messages = messageService.getUserMessages(userId);
            List<MessageDto> messageDtos = messages.stream()
                .map(MessageDto::new)
                .collect(Collectors.toList());
            return ResponseEntity.ok(messageDtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Send a message (frontend expects this endpoint)
    @PostMapping
    public ResponseEntity<MessageDto> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            Message message = messageService.sendMessage(
                currentUser.getId(),
                request.getBookId(),
                request.getContent(),
                request.getParentMessageId(),
                request.getRecipientId()
            );
            return ResponseEntity.ok(new MessageDto(message));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Delete a message (soft delete)
    @DeleteMapping("/{messageId}")
    public ResponseEntity<Void> deleteMessage(@PathVariable String messageId) {
        try {
            User currentUser = authenticatedUserService.getCurrentUser();

            boolean deleted = messageService.deleteMessage(messageId, currentUser.getId());
            return deleted ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

}
