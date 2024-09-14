package com.project.telegramclientapi.telegram.v2.chat.repository;

import com.project.telegramclientapi.telegram.v2.chat.model.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ChatRepository extends MongoRepository<Chat, String> {
}
