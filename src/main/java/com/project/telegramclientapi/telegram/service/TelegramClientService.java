package com.project.telegramclientapi.telegram.service;

import com.project.telegramclientapi.chat.model.Chat;
import com.project.telegramclientapi.chat.repository.ChatRepository;
import com.project.telegramclientapi.telegram.TelegramMessageHandler;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class TelegramClientService {

    private final ChatRepository chatRepository;
    private final TelegramMessageHandler telegramMessageHandler;
    private final SimpleTelegramClient telegramClient;

    public void onUpdateHandler(TdApi.UpdateNewMessage incomingMessage) {
        TdApi.Message message = incomingMessage.message;
        TdApi.MessageContent messageContent = message.content;
        log.info("onUpdateHandler");

        handleUpdate(message, messageContent);
    }

    private void handleUpdate(TdApi.Message message, TdApi.MessageContent messageContent) {
        Chat chat = new Chat();
        List<byte[]> images = new ArrayList<>();
        List<String> pathToFiles = new ArrayList<>();

        telegramMessageHandler.fillWithCommonData(chat, message);
        telegramMessageHandler.processMessageSenderUserData(message, chat);
        telegramMessageHandler.processMessageTextData(messageContent, chat);
        telegramMessageHandler.processMessagePhotoData(messageContent, chat, pathToFiles, images);

        chatRepository.save(chat);
    }

    public TelegramClientService(SimpleTelegramClient simpleTelegramClient, ChatRepository chatRepository, TelegramMessageHandler telegramMessageHandler) {
        simpleTelegramClient.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);
        this.telegramClient = simpleTelegramClient;
        this.chatRepository = chatRepository;
        this.telegramMessageHandler = telegramMessageHandler;
    }

}
