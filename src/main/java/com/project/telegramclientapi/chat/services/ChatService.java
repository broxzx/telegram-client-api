package com.project.telegramclientapi.chat.services;

import com.project.telegramclientapi.chat.data.Chat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;

    public void saveChat(Chat chat) {
        chatRepository.save(chat);
    }

    public List<Chat> getAllChatsByUserId(String chatId) {
        return chatRepository.findByChatIdOrderByTime(chatId);
    }

}
