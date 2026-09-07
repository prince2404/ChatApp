package com.chat.app.service;

import com.chat.app.model.ChatRoom;
import com.chat.app.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    // Alphanumeric characters excluding visually ambiguous ones (0/O, 1/I)
    private static final String ROOM_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * Generates a random 6-character uppercase room ID, saves it to MongoDB, and returns it.
     */
    public String generateAndSaveRoom() {
        String roomId;
        do {
            roomId = generateRandomCode(6);
        } while (chatRoomRepository.existsByRoomId(roomId));

        ChatRoom room = ChatRoom.builder()
                .roomId(roomId)
                .createdAt(LocalDateTime.now())
                .build();

        chatRoomRepository.save(room);
        log.info("Saved new room to MongoDB: {}", roomId);
        return roomId;
    }

    /**
     * Checks if a room exists in MongoDB.
     */
    public boolean roomExists(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return false;
        }
        return chatRoomRepository.existsByRoomId(roomId.trim().toUpperCase());
    }

    public Optional<ChatRoom> getRoomByRoomId(String roomId) {
        if (roomId == null || roomId.trim().isEmpty()) {
            return Optional.empty();
        }
        return chatRoomRepository.findByRoomId(roomId.trim());
    }

    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAllByOrderByCreatedAtAsc();
    }

    public ChatRoom createRoom(String roomId, String name, String description, String createdBy) {
        String targetRoomId = (roomId != null && !roomId.trim().isEmpty())
                ? roomId.trim()
                : generateRandomCode(6);

        if (chatRoomRepository.existsByRoomId(targetRoomId)) {
            throw new IllegalArgumentException("Room ID already exists: " + targetRoomId);
        }

        ChatRoom room = ChatRoom.builder()
                .roomId(targetRoomId)
                .name(name)
                .description(description)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        return chatRoomRepository.save(room);
    }

    private String generateRandomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ROOM_CHARS.charAt(RANDOM.nextInt(ROOM_CHARS.length())));
        }
        return sb.toString();
    }
}
