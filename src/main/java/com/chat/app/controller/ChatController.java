package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import com.chat.app.model.MessageType;
import com.chat.app.service.ChatMessageService;
import com.chat.app.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller handling STOMP WebSocket messaging and room endpoints.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    // ── WebSocket: Room Messaging ─────────────────────────────────────────────
    /**
     * Receives messages sent to "/app/chat/{roomId}".
     * Broadcasts the saved message directly to subscribers on "/topic/{roomId}".
     */
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, @Payload ChatMessage message) {
        // Enforce room code on message in uppercase
        message.setRoomId(roomId.toUpperCase());

        if (message.getType() == null) {
            message.setType(MessageType.CHAT);
        }

        try {
            return chatMessageService.save(message);
        } catch (Exception e) {
            log.warn("MongoDB write failed, broadcasting message without persistence: {}", e.getMessage());
            message.setTimestamp(LocalDateTime.now());
            return message;
        }
    }

    // ── REST: Room Message History ────────────────────────────────────────────
    /**
     * Loads up to 50 previous messages for a specific room.
     */
    @GetMapping("/api/messages/{roomId}")
    @ResponseBody
    public List<ChatMessage> getRoomMessages(@PathVariable String roomId) {
        return chatMessageService.getLast50MessagesByRoom(roomId);
    }

    // ── REST: Create Room ─────────────────────────────────────────────────────
    /**
     * Generates a random 6-character uppercase room ID, saves it to MongoDB, and returns it.
     * Supports both POST (preferred) and GET.
     */
    @RequestMapping(value = "/api/room/create", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public Map<String, String> createRoom() {
        String generatedRoomId = chatRoomService.generateAndSaveRoom();
        log.info("Created new room: {}", generatedRoomId);
        return Map.of("roomId", generatedRoomId);
    }

    // ── REST: Check Room Existence ────────────────────────────────────────────
    /**
     * Checks if a room exists in MongoDB before allowing a user to join.
     * Accessible via GET /api/room/{roomId}/exists and GET /api/room/check/{roomId}.
     */
    @GetMapping({"/api/room/{roomId}/exists", "/api/room/check/{roomId}"})
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> checkRoomExists(@PathVariable String roomId) {
        boolean exists = chatRoomService.roomExists(roomId);
        if (exists) {
            return ResponseEntity.ok(Map.of("exists", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("exists", false));
        }
    }

    // ── Web Navigation Endpoints ──────────────────────────────────────────────
    @GetMapping("/")
    public String root() {
        return "redirect:/chat";
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }
}