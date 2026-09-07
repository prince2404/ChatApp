package com.chat.app.repository;

import com.chat.app.model.ChatRoom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends MongoRepository<ChatRoom, String> {
    Optional<ChatRoom> findByRoomId(String roomId);
    boolean existsByRoomId(String roomId);
    List<ChatRoom> findAllByOrderByCreatedAtAsc();
}
