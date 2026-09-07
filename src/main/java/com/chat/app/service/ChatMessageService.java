package com.chat.app.service;

import com.chat.app.model.ChatMessage;
import com.chat.app.model.MessageType;
import com.chat.app.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service handling business logic for saving and retrieving chat messages.
 */
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final MessageRepository messageRepository;

    /**
     * Persists a chat message into MongoDB:
     * 1. Validates or sets default roomId ("general") if absent
     * 2. Sets default MessageType to CHAT
     * 3. Sets server-side timestamp (never trust client clock)
     * 4. Saves and returns the document with the generated MongoDB ID
     *
     * @param message the incoming chat message
     * @return the saved ChatMessage document
     */
    public ChatMessage save(ChatMessage message) {
        if (message.getRoomId() == null || message.getRoomId().trim().isEmpty()) {
            message.setRoomId("general");
        } else {
            message.setRoomId(message.getRoomId().trim());
        }
        if (message.getType() == null) {
            message.setType(MessageType.CHAT);
        }
        // Always assign timestamp on server
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    /**
     * Retrieves the last 50 messages for a specific room.
     * Oldest messages appear first so the frontend renders them top-to-bottom.
     *
     * @param roomId the room identifier
     * @return list of up to 50 messages
     */
    public List<ChatMessage> getLast50MessagesByRoom(String roomId) {
        String targetRoom = (roomId == null || roomId.trim().isEmpty()) ? "general" : roomId.trim();
        return messageRepository.findTop50ByRoomIdOrderByTimestampAsc(targetRoom);
    }

    /**
     * Backward-compatible global message history fetch.
     */
    public List<ChatMessage> getLast50Messages() {
        return messageRepository.findTop50ByOrderByTimestampAsc();
    }
}
