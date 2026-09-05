package com.chat.app.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "messages")
public class ChatMessage {
    @Id
    // ↑ MongoDB uses String IDs (ObjectId format like "507f1f77bcf86cd799439011")
    //   Changed from Long because MongoDB generates its own IDs.
    private String id;

    private String sender;

    private String content;

    @Indexed
    // ↑ Creates an index on timestamp in MongoDB.
    //   Makes "find last 50 messages ordered by time" queries fast.
    private LocalDateTime timestamp;
}
