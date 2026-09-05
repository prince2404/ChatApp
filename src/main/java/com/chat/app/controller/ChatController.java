package com.chat.app.controller;

import com.chat.app.model.ChatMessage;
import com.chat.app.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    // ↑ Spring injects this automatically via @RequiredArgsConstructor

    // ── WebSocket: receive and broadcast message ──────────────
    @MessageMapping("/sendMessage")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        try {
            return chatMessageService.save(message);  // try to save
        } catch (Exception e) {
            // If MongoDB fails, still broadcast — don't block the chat
            message.setTimestamp(java.time.LocalDateTime.now());
            return message;
        }
    }

    // ── REST: load message history ────────────────────────────
    @GetMapping("/api/messages")
    @ResponseBody
    // ↑ @ResponseBody tells Spring to convert the List<ChatMessage>
    //   directly to JSON and write it to the HTTP response.
    //   Without it, Spring would try to find a template named
    //   after the return value.
    public List<ChatMessage> getMessageHistory() {
        return chatMessageService.getLast50Messages();
        // ↑ Called once by the browser on page load via fetch().
        //   Returns last 50 messages as a JSON array.
    }

    // ── HTTP: serve chat page ─────────────────────────────────
    @GetMapping("/")
    public String root() {
        return "redirect:/chat";
    }

    @GetMapping("chat")
    public String chat() {
        return "chat";
    }
}