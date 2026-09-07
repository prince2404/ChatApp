package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import com.chat.app.model.MessageType;
import com.chat.app.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // ── Multi-Room WebSocket: Send message to dynamic room ─────
    @MessageMapping("/chat.sendMessage/{roomId}")
    public void sendRoomMessage(@DestinationVariable String roomId,
                                @Payload ChatMessage message,
                                Principal principal) {
        // Enforce authenticated sender identity from JWT Principal (prevents spoofing)
        if (principal != null) {
            message.setSender(principal.getName());
        }
        message.setRoomId(roomId);
        if (message.getType() == null) {
            message.setType(MessageType.CHAT);
        }

        ChatMessage savedMessage;
        try {
            savedMessage = chatMessageService.save(message);
        } catch (Exception e) {
            log.warn("Could not persist message to MongoDB, broadcasting anyway: {}", e.getMessage());
            message.setTimestamp(LocalDateTime.now());
            savedMessage = message;
        }

        messagingTemplate.convertAndSend("/topic/" + roomId, savedMessage);
    }

    // ── Multi-Room WebSocket: Announce user join event ────────
    @MessageMapping("/chat.addUser/{roomId}")
    public void addUserToRoom(@DestinationVariable String roomId,
                              @Payload ChatMessage message,
                              Principal principal) {
        if (principal != null) {
            message.setSender(principal.getName());
        }
        message.setRoomId(roomId);
        message.setType(MessageType.JOIN);
        message.setContent(message.getSender() + " joined #" + roomId);
        message.setTimestamp(LocalDateTime.now());

        log.info("User {} joined room #{}", message.getSender(), roomId);
        messagingTemplate.convertAndSend("/topic/" + roomId, message);
    }

    // ── Backward-compatible global WebSocket endpoint ─────────
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message, Principal principal) {
        if (principal != null) {
            message.setSender(principal.getName());
        }
        if (message.getRoomId() == null || message.getRoomId().trim().isEmpty()) {
            message.setRoomId("general");
        }
        try {
            return chatMessageService.save(message);
        } catch (Exception e) {
            log.warn("Could not persist legacy message: {}", e.getMessage());
            message.setTimestamp(LocalDateTime.now());
            return message;
        }
    }

    // ── REST: load message history (room-aware) ───────────────
    @GetMapping("/api/messages")
    @ResponseBody
    public List<ChatMessage> getMessageHistory(@RequestParam(required = false) String roomId) {
        if (roomId != null && !roomId.trim().isEmpty()) {
            return chatMessageService.getLast50MessagesByRoom(roomId);
        }
        return chatMessageService.getLast50Messages();
    }

    // ── HTTP: serve chat page ─────────────────────────────────
    @GetMapping("/")
    public String root() {
        return "redirect:/chat";
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }
}