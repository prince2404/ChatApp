package com.chat.app.service;

import com.chat.app.model.ChatMessage;
import com.chat.app.model.MessageType;
import com.chat.app.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final MessageRepository messageRepository;

    // Save a message to MongoDB with server-assigned timestamp and normalized room
    public ChatMessage save(ChatMessage message) {
        if (message.getRoomId() == null || message.getRoomId().trim().isEmpty()) {
            message.setRoomId("general");
        }
        if (message.getType() == null) {
            message.setType(MessageType.CHAT);
        }
        message.setTimestamp(LocalDateTime.now());
        return messageRepository.save(message);
    }

    // Get last 50 messages for a specific room
    public List<ChatMessage> getLast50MessagesByRoom(String roomId) {
        String targetRoom = (roomId == null || roomId.trim().isEmpty()) ? "general" : roomId.trim();
        return messageRepository.findTop50ByRoomIdOrderByTimestampAsc(targetRoom);
    }

    // Get last 50 global messages for backward compatibility
    public List<ChatMessage> getLast50Messages() {
        return messageRepository.findTop50ByOrderByTimestampAsc();
    }
}
