package com.project.telegramclientapi.chat;

import com.project.telegramclientapi.chat.data.Chat;
import com.project.telegramclientapi.chat.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/chat/")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/{chatId}")
    public ResponseEntity<List<Chat>> getMessagesByUserId(@PathVariable("chatId") String chatId) {
        return ResponseEntity.ok(chatService.getAllChatsByUserId(chatId));
    }

    @GetMapping("/front")
    public String getChat(Model model) {
        List<Chat> chats = List.of(
                new Chat("1", "user1", "user2", "Hello, how are you?", 1728931944, null, null, LocalDateTime.now()),
                new Chat("2", "user2", "user1", "I'm fine, thanks! How about you?", 1728931945, null, null, LocalDateTime.now())
        );

        model.addAttribute("chats", chats);
        return "chat";
    }

}
