package com.chat.app.service;

import com.chat.app.model.ChatRoom;
import com.chat.app.repository.ChatRoomRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @PostConstruct
    public void initDefaultRooms() {
        try {
            seedRoomIfAbsent("general", "General Chat", "General conversation for everyone", "system");
            seedRoomIfAbsent("tech-talk", "Tech Talk", "Discuss Java, Spring Boot, MongoDB & architecture", "system");
            seedRoomIfAbsent("random", "Random", "Casual banter, off-topic chat & fun", "system");
        } catch (Exception e) {
            log.warn("Could not seed default rooms (MongoDB may be connecting or read-only): {}", e.getMessage());
        }
    }

    private void seedRoomIfAbsent(String roomId, String name, String description, String createdBy) {
        if (!chatRoomRepository.existsByRoomId(roomId)) {
            ChatRoom room = ChatRoom.builder()
                    .roomId(roomId)
                    .name(name)
                    .description(description)
                    .createdBy(createdBy)
                    .createdAt(LocalDateTime.now())
                    .build();
            chatRoomRepository.save(room);
            log.info("Initialized default chat room: #{}", roomId);
        }
    }

    public List<ChatRoom> getAllRooms() {
        return chatRoomRepository.findAllByOrderByCreatedAtAsc();
    }

    public Optional<ChatRoom> getRoomByRoomId(String roomId) {
        return chatRoomRepository.findByRoomId(roomId);
    }

    public ChatRoom createRoom(String rawRoomId, String name, String description, String createdBy) {
        String normalizedRoomId = rawRoomId.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "-");
        if (normalizedRoomId.isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be empty");
        }
        if (chatRoomRepository.existsByRoomId(normalizedRoomId)) {
            throw new IllegalArgumentException("Room with ID #" + normalizedRoomId + " already exists");
        }

        ChatRoom room = ChatRoom.builder()
                .roomId(normalizedRoomId)
                .name((name == null || name.trim().isEmpty()) ? normalizedRoomId : name.trim())
                .description(description)
                .createdBy(createdBy)
                .createdAt(LocalDateTime.now())
                .build();

        return chatRoomRepository.save(room);
    }
}
