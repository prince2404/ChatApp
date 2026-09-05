package com.chat.app.service;

import com.chat.app.model.ChatMessage;
import com.chat.app.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
// ↑ Lombok: generates constructor with all final fields.
//   Spring uses this constructor to inject MessageRepository.
public class ChatMessageService {

    private final MessageRepository messageRepository;

    // Save a message to MongoDB
    public ChatMessage save(ChatMessage message) {
        message.setTimestamp(LocalDateTime.now());
        // ↑ We set the timestamp here on the server side.
        //   Never trust the client to send timestamps —
        //   they can be wrong or manipulated.
        return messageRepository.save(message);
        // ↑ save() inserts if new (no ID), updates if ID exists.
        //   Returns the saved object with the auto-generated ID filled in.
    }

    // Get last 50 messages for history
    public List<ChatMessage> getLast50Messages() {
        return messageRepository.findTop50ByOrderByTimestampAsc();
        // ↑ Returns oldest-first so UI renders them top to bottom
        //   in the correct chronological order.
    }
}
