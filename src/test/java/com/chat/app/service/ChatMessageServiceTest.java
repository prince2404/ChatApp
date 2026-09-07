package com.chat.app.service;

import com.chat.app.model.ChatMessage;
import com.chat.app.model.MessageType;
import com.chat.app.repository.MessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private ChatMessageService chatMessageService;

    private ChatMessage testMessage;

    @BeforeEach
    void setUp() {
        testMessage = ChatMessage.builder()
                .sender("alice")
                .content("Hello world!")
                .roomId("tech-talk")
                .type(MessageType.CHAT)
                .build();
    }

    @Test
    void testSaveMessageSetsTimestampAndDefaults() {
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage messageWithoutRoom = ChatMessage.builder()
                .sender("bob")
                .content("Hey there")
                .build();

        ChatMessage saved = chatMessageService.save(messageWithoutRoom);

        assertNotNull(saved.getTimestamp());
        assertEquals("general", saved.getRoomId());
        assertEquals(MessageType.CHAT, saved.getType());
        verify(messageRepository, times(1)).save(any(ChatMessage.class));
    }

    @Test
    void testGetLast50MessagesByRoom() {
        when(messageRepository.findTop50ByRoomIdOrderByTimestampAsc("tech-talk"))
                .thenReturn(List.of(testMessage));

        List<ChatMessage> messages = chatMessageService.getLast50MessagesByRoom("tech-talk");

        assertEquals(1, messages.size());
        assertEquals("tech-talk", messages.get(0).getRoomId());
        assertEquals("alice", messages.get(0).getSender());
        verify(messageRepository, times(1)).findTop50ByRoomIdOrderByTimestampAsc("tech-talk");
    }

    @Test
    void testGetLast50MessagesByBlankRoomDefaultsToGeneral() {
        when(messageRepository.findTop50ByRoomIdOrderByTimestampAsc("general"))
                .thenReturn(List.of());

        List<ChatMessage> messages = chatMessageService.getLast50MessagesByRoom("   ");

        assertNotNull(messages);
        verify(messageRepository, times(1)).findTop50ByRoomIdOrderByTimestampAsc("general");
    }
}
