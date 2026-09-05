package com.chat.app.repository;

import com.chat.app.model.ChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<ChatMessage, String> {
    // MongoRepository<ChatMessage, String>
    // ↑ First type  = the document class
    //   Second type = the ID type (String for MongoDB ObjectId)
    // Spring Data auto-generates all CRUD methods — no SQL needed.

    List<ChatMessage> findTop50ByOrderByTimestampAsc();
    // ↑ Spring Data reads this method name and builds the query:
    //   "find top 50 documents, ordered by timestamp ascending"
    //   No query code needed — the method name IS the query.
    //   Ascending = oldest first, so history loads in correct order.
}
