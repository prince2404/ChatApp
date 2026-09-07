package com.chat.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "rooms")
public class ChatRoom {
    @Id
    private String id;

    // 6-character uppercase room identifier (e.g. "XK92PL")
    @Indexed(unique = true)
    private String roomId;

    private String name;
    private String description;
    private String createdBy;
    private LocalDateTime createdAt;
}
