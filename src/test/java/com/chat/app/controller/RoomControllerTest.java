package com.chat.app.controller;

import com.chat.app.dto.RoomRequest;
import com.chat.app.model.ChatMessage;
import com.chat.app.model.ChatRoom;
import com.chat.app.service.ChatMessageService;
import com.chat.app.service.ChatRoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoomControllerTest {

    @Mock
    private ChatRoomService chatRoomService;

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private RoomController roomController;

    @Test
    void testGetAllRooms() {
        ChatRoom r = ChatRoom.builder().roomId("general").name("General").build();
        when(chatRoomService.getAllRooms()).thenReturn(List.of(r));

        ResponseEntity<List<ChatRoom>> response = roomController.getAllRooms();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetRoomByIdFound() {
        ChatRoom r = ChatRoom.builder().roomId("general").name("General").build();
        when(chatRoomService.getRoomByRoomId("general")).thenReturn(Optional.of(r));

        ResponseEntity<?> response = roomController.getRoom("general");

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetRoomByIdNotFound() {
        when(chatRoomService.getRoomByRoomId("nonexistent")).thenReturn(Optional.empty());

        ResponseEntity<?> response = roomController.getRoom("nonexistent");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testCreateRoomSuccess() {
        RoomRequest request = RoomRequest.builder()
                .roomId("gaming")
                .name("Gaming Channel")
                .description("Gaming talk")
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("alice");

        ChatRoom created = ChatRoom.builder()
                .roomId("gaming")
                .name("Gaming Channel")
                .createdBy("alice")
                .build();

        when(chatRoomService.createRoom("gaming", "Gaming Channel", "Gaming talk", "alice"))
                .thenReturn(created);

        ResponseEntity<?> response = roomController.createRoom(request, auth);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testGetRoomMessages() {
        ChatMessage msg = ChatMessage.builder().content("Hi").roomId("general").build();
        when(chatMessageService.getLast50MessagesByRoom("general")).thenReturn(List.of(msg));

        ResponseEntity<List<ChatMessage>> response = roomController.getRoomMessages("general");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}
