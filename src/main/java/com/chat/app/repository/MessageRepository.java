package com.chat.app.repository;

import com.chat.app.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data MongoDB Repository for ChatMessage documents.
 */
@Repository
public interface MessageRepository extends MongoRepository<ChatMessage, String> {

    /**
     * Spring Data parses this method signature to construct the MongoDB query:
     * - Filter by 'roomId'
     * - Sort by 'timestamp' ascending (oldest first)
     * - Limit to top 50 documents
     *
     * @param roomId the unique 6-character room identifier
     * @return up to 50 messages for the specified room in chronological order
     */
    List<ChatMessage> findTop50ByRoomIdOrderByTimestampAsc(String roomId);

    /**
     * Legacy global message history query (for backward compatibility).
     */
    List<ChatMessage> findTop50ByOrderByTimestampAsc();
}
