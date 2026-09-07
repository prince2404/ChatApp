package com.chat.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document entity representing a single chat message.
 * Stored in the "messages" collection.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "messages")
// Compound index ensures that queries filtering by roomId and sorted by timestamp are fast
@CompoundIndex(name = "room_timestamp_idx", def = "{'roomId': 1, 'timestamp': 1}")
public class ChatMessage {

    @Id
    // MongoDB auto-generated unique ID (ObjectId string format)
    private String id;

    @Indexed
    // The room this message belongs to (e.g. "XK92PL"). Ensures message isolation per room
    private String roomId;

    // Display name of the user who sent the message
    private String sender;

    // The text content of the chat message
    private String content;

    // Optional message type (CHAT, JOIN, LEAVE) for system notifications
    private MessageType type;

    @Indexed
    // Server-assigned timestamp to ensure reliable chronological ordering
    private LocalDateTime timestamp;
}
