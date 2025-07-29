package com.example.hackathon.controller;

import com.example.hackathon.dto.ApiResponse;
import com.example.hackathon.dto.SendMessageRequest;
import com.example.hackathon.dto.UserResponse;
import com.example.hackathon.model.ChatMessage;
import com.example.hackathon.model.ChatConversation;
import com.example.hackathon.model.User;
import com.example.hackathon.repository.ChatMessageRepository;
import com.example.hackathon.repository.ChatConversationRepository;
import com.example.hackathon.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatConversationRepository chatConversationRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody SendMessageRequest request) {
        try {
            ChatMessage message = new ChatMessage(
                request.getSenderId(),
                request.getReceiverId(),
                request.getMessage()
            );
            chatMessageRepository.save(message);
            
            // Update or create conversation
            updateConversation(request.getSenderId(), request.getReceiverId(), request.getMessage());
            
            return ResponseEntity.ok(new ApiResponse(true, "Message sent successfully!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Failed to send message: " + e.getMessage()));
        }
    }

    @GetMapping("/messages/{userId1}/{userId2}")
    public ResponseEntity<List<ChatMessage>> getMessages(
            @PathVariable String userId1,
            @PathVariable String userId2) {
        List<ChatMessage> messages = chatMessageRepository.findMessagesBetweenUsers(userId1, userId2);
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversations/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getUserConversations(@PathVariable String userId) {
        try {
            List<ChatConversation> conversations = chatConversationRepository.findConversationsByUserId(userId);
            
            List<Map<String, Object>> conversationData = conversations.stream()
                .map(conv -> {
                    Map<String, Object> data = new HashMap<>();
                    
                    // Determine the other user
                    String otherUserId = conv.getUser1Id().equals(userId) ? conv.getUser2Id() : conv.getUser1Id();
                    Optional<User> otherUser = userRepository.findById(otherUserId);
                    
                    if (otherUser.isPresent()) {
                        data.put("conversationId", conv.getId());
                        data.put("otherUser", new UserResponse(otherUser.get()));
                        data.put("lastMessage", conv.getLastMessage());
                        data.put("lastMessageTime", conv.getLastMessageTime());
                        data.put("unreadCount", conv.getUser1Id().equals(userId) ? 
                            conv.getUnreadCountUser1() : conv.getUnreadCountUser2());
                    }
                    
                    return data;
                })
                .filter(data -> data.containsKey("otherUser"))
                .sorted((a, b) -> {
                    LocalDateTime timeA = (LocalDateTime) a.get("lastMessageTime");
                    LocalDateTime timeB = (LocalDateTime) b.get("lastMessageTime");
                    return timeB != null && timeA != null ? timeB.compareTo(timeA) : 0;
                })
                .collect(Collectors.toList());
                
            return ResponseEntity.ok(conversationData);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/conversations/{conversationId}/read")
    public ResponseEntity<?> markConversationAsRead(@PathVariable String conversationId, 
                                                   @RequestParam String userId) {
        try {
            Optional<ChatConversation> convOpt = chatConversationRepository.findById(conversationId);
            if (convOpt.isPresent()) {
                ChatConversation conversation = convOpt.get();
                
                if (conversation.getUser1Id().equals(userId)) {
                    conversation.setUnreadCountUser1(0);
                } else if (conversation.getUser2Id().equals(userId)) {
                    conversation.setUnreadCountUser2(0);
                }
                
                chatConversationRepository.save(conversation);
                return ResponseEntity.ok(new ApiResponse(true, "Conversation marked as read"));
            }
            return ResponseEntity.badRequest().body(new ApiResponse(false, "Conversation not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new ApiResponse(false, "Failed to mark conversation as read"));
        }
    }

    private void updateConversation(String senderId, String receiverId, String message) {
        Optional<ChatConversation> existingConv = chatConversationRepository
            .findConversationBetweenUsers(senderId, receiverId);
            
        ChatConversation conversation;
        if (existingConv.isPresent()) {
            conversation = existingConv.get();
        } else {
            conversation = new ChatConversation(senderId, receiverId);
        }
        
        conversation.setLastMessage(message);
        conversation.setLastSenderId(senderId);
        conversation.setLastMessageTime(LocalDateTime.now());
        conversation.setUpdatedAt(LocalDateTime.now());
        
        // Increment unread count for receiver
        if (conversation.getUser1Id().equals(receiverId)) {
            conversation.setUnreadCountUser1(conversation.getUnreadCountUser1() + 1);
        } else {
            conversation.setUnreadCountUser2(conversation.getUnreadCountUser2() + 1);
        }
        
        chatConversationRepository.save(conversation);
    }
}