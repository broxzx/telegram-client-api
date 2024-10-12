package com.project.telegramclientapi.chat.repository;

import com.project.telegramclientapi.chat.model.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, String> {
}
