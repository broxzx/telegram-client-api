package com.project.telegramclientapi.chat.services;

import com.project.telegramclientapi.chat.data.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, String> {

    List<Chat> findByChatIdOrderByTime(String chatId);

}
