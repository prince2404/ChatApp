package com.chat.app.service;

import com.chat.app.model.ChatRoom;
import com.chat.app.repository.ChatRoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    void testGetAllRooms() {
        ChatRoom room = ChatRoom.builder().roomId("general").name("General").build();
        when(chatRoomRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(room));

        List<ChatRoom> rooms = chatRoomService.getAllRooms();

        assertEquals(1, rooms.size());
        assertEquals("general", rooms.get(0).getRoomId());
    }

    @Test
    void testCreateRoomNormalizesId() {
        when(chatRoomRepository.existsByRoomId("dev-chat")).thenReturn(false);
        when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(i -> i.getArgument(0));

        ChatRoom created = chatRoomService.createRoom("dev-chat", "Dev Chat", "Developers hangout", "alice");

        assertEquals("dev-chat", created.getRoomId());
        assertEquals("alice", created.getCreatedBy());
    }

    @Test
    void testCreateDuplicateRoomThrowsException() {
        when(chatRoomRepository.existsByRoomId("general")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                chatRoomService.createRoom("general", "General", "Desc", "alice")
        );
    }
}
