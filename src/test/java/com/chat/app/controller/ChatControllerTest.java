package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import com.chat.app.service.ChatMessageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatMessageService chatMessageService;

    @InjectMocks
    private ChatController chatController;

    @Test
    void testCreateRoomReturnsSixCharUppercaseId() {
        Map<String, String> response = chatController.createRoom();

        assertNotNull(response);
        assertTrue(response.containsKey("roomId"));

        String roomId = response.get("roomId");
        assertEquals(6, roomId.length());
        assertTrue(roomId.matches("^[A-Z0-9]{6}$"));
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
