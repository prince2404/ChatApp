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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Main controller for STOMP WebSocket messaging and room-related REST endpoints.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;

    // Set of uppercase letters and digits used for 6-character room IDs (avoids ambiguous 0/O, 1/I)
    private static final String ROOM_ID_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ── WebSocket: Room Messaging ─────────────────────────────────────────────
    /**
     * Handles messages sent to "/app/chat/{roomId}".
     * Broadcasts the saved message directly to "/topic/{roomId}".
     *
     * @param roomId  the destination room extracted from the dynamic destination URL
     * @param message the chat payload containing sender and content
     * @return the persisted message containing server timestamp and database ID
     */
    @MessageMapping("/chat/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatMessage sendMessage(@DestinationVariable String roomId, @Payload ChatMessage message) {
        // Enforce the room ID onto the message entity
        message.setRoomId(roomId.toUpperCase());

        if (message.getType() == null) {
            message.setType(MessageType.CHAT);
        }

        try {
            // Persist message to MongoDB Atlas
            return chatMessageService.save(message);
        } catch (Exception e) {
            log.warn("MongoDB write failed, broadcasting message without persistence: {}", e.getMessage());
            message.setTimestamp(LocalDateTime.now());
            return message;
        }
    }

    // ── REST: Room Message History ────────────────────────────────────────────
    /**
     * Endpoint called by the browser when joining a room to fetch previous chat history.
     * Returns up to 50 messages strictly belonging to the given roomId.
     *
     * @param roomId the room identifier
     * @return list of ChatMessage documents in chronological order
     */
    @GetMapping("/api/messages/{roomId}")
    @ResponseBody
    public List<ChatMessage> getRoomMessages(@PathVariable String roomId) {
        return chatMessageService.getLast50MessagesByRoom(roomId);
    }

    // ── REST: Create Room ─────────────────────────────────────────────────────
    /**
     * Generates and returns a random 6-character uppercase room ID (e.g. "XK92PL").
     *
     * @return JSON response containing {"roomId": "..."}
     */
    @GetMapping("/api/room/create")
    @ResponseBody
    public Map<String, String> createRoom() {
        String generatedRoomId = generateRandomRoomId(6);
        log.info("Generated new room ID: {}", generatedRoomId);
        return Map.of("roomId", generatedRoomId);
    }

    /**
     * Generates a secure, cryptographically random uppercase alphanumeric string.
     */
    private String generateRandomRoomId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = SECURE_RANDOM.nextInt(ROOM_ID_CHARACTERS.length());
            sb.append(ROOM_ID_CHARACTERS.charAt(randomIndex));
        }
        return sb.toString();
    }

    // ── Web Navigation Endpoints ──────────────────────────────────────────────
    /**
     * Redirects root "/" to the chat view.
     */
    @GetMapping("/")
    public String root() {
        return "redirect:/chat";
    }

    /**
     * Serves the single-page chat template (lobby + room interface).
     */
    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }
}