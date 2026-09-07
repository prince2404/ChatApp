package com.chat.app.repository;

import com.chat.app.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<ChatMessage, String> {

    // Retrieve last 50 messages for a specific room ordered by timestamp ascending
    List<ChatMessage> findTop50ByRoomIdOrderByTimestampAsc(String roomId);

    // Backward-compatible query for global messages
    List<ChatMessage> findTop50ByOrderByTimestampAsc();
}
