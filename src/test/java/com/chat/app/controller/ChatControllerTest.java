package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import com.chat.app.service.ChatMessageService;
import com.chat.app.service.ChatRoomService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @Mock
    private ChatRoomService chatRoomService;

    @InjectMocks
    private ChatController chatController;

    @Test
    void testCreateRoomCallsServiceAndReturnsId() {
        when(chatRoomService.generateAndSaveRoom()).thenReturn("XK92PL");

        Map<String, String> response = chatController.createRoom();

        assertNotNull(response);
        assertEquals("XK92PL", response.get("roomId"));
        verify(chatRoomService, times(1)).generateAndSaveRoom();
    }

    @Test
    void testCheckRoomExists() {
        when(chatRoomService.roomExists("XK92PL")).thenReturn(true);

        ResponseEntity<Map<String, Boolean>> response = chatController.checkRoom("XK92PL");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("exists"));
    }

    @Test
    void testCheckRoomDoesNotExist() {
        when(chatRoomService.roomExists("NOPE99")).thenReturn(false);

        ResponseEntity<Map<String, Boolean>> response = chatController.checkRoom("NOPE99");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertFalse(response.getBody().get("exists"));
    }

    @Test
    void testGetRoomMessages() {
        ChatMessage msg = ChatMessage.builder().sender("Alice").content("Hello").roomId("XK92PL").build();
        when(chatMessageService.getLast50MessagesByRoom("XK92PL")).thenReturn(List.of(msg));

        List<ChatMessage> result = chatController.getRoomMessages("XK92PL");

        assertEquals(1, result.size());
        assertEquals("XK92PL", result.get(0).getRoomId());
        verify(chatMessageService, times(1)).getLast50MessagesByRoom("XK92PL");
    }

    @Test
    void testSendMessageSetsRoomIdAndSaves() {
        ChatMessage incoming = ChatMessage.builder().sender("Bob").content("Hey").build();
        ChatMessage saved = ChatMessage.builder().id("123").sender("Bob").content("Hey").roomId("XK92PL").build();

        when(chatMessageService.save(any(ChatMessage.class))).thenReturn(saved);

        ChatMessage result = chatController.sendMessage("XK92PL", incoming);

        assertNotNull(result);
        assertEquals("XK92PL", result.getRoomId());
        verify(chatMessageService, times(1)).save(incoming);
    }
}
