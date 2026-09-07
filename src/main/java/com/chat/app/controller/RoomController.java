package com.chat.app.controller;

import com.chat.app.dto.ApiResponse;
import com.chat.app.dto.RoomRequest;
import com.chat.app.model.ChatMessage;
import com.chat.app.model.ChatRoom;
import com.chat.app.service.ChatMessageService;
import com.chat.app.service.ChatRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;

    // List all available chat rooms
    @GetMapping
    public ResponseEntity<List<ChatRoom>> getAllRooms() {
        return ResponseEntity.ok(chatRoomService.getAllRooms());
    }

    // Get room details by roomId
    @GetMapping("/{roomId}")
    public ResponseEntity<?> getRoom(@PathVariable String roomId) {
        return chatRoomService.getRoomByRoomId(roomId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // Create a new room
    @PostMapping
    public ResponseEntity<?> createRoom(@Valid @RequestBody RoomRequest request, Authentication authentication) {
        String createdBy = (authentication != null) ? authentication.getName() : "anonymous";
        try {
            ChatRoom created = chatRoomService.createRoom(
                    request.getRoomId(),
                    request.getName(),
                    request.getDescription(),
                    createdBy
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    // Retrieve the last 50 messages for a specific room
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessage>> getRoomMessages(@PathVariable String roomId) {
        return ResponseEntity.ok(chatMessageService.getLast50MessagesByRoom(roomId));
    }
}
