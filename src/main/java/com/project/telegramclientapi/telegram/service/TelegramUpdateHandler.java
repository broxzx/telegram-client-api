package com.project.telegramclientapi.telegram.service;

import com.project.telegramclientapi.chat.repository.ChatRepository;
import com.project.telegramclientapi.telegram.TelegramMessageProcessor;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TelegramUpdateHandler {

    private final ChatRepository chatRepository;
    private final SimpleTelegramClient telegramClient;

    public void onUpdateHandler(TdApi.UpdateNewMessage incomingMessage) {
        TdApi.Message message = incomingMessage.message;
        TdApi.MessageContent messageContent = message.content;
        log.info("onUpdateHandler");

        handleUpdate(message, messageContent);
    }

    private void handleUpdate(TdApi.Message message, TdApi.MessageContent messageContent) {
        TelegramMessageProcessor telegramMessageProcessor = new TelegramMessageProcessor(telegramClient);

        telegramMessageProcessor.fillWithCommonData(message);

        telegramMessageProcessor.processMessagePhotoData(messageContent)
                .thenRun(() -> {
                    telegramMessageProcessor.processMessageSenderUserData(message);
                    telegramMessageProcessor.processMessageTextData(messageContent);

                    chatRepository.save(telegramMessageProcessor.getChat());
                    log.info("Chat saved with pathToFiles: {}", telegramMessageProcessor.getChat().getPathToFiles());
                })
                .exceptionally(ex -> {
                    log.error("Error while processing message: {}", ex.getMessage());
                    return null;
                });
    }

    public TelegramUpdateHandler(SimpleTelegramClient simpleTelegramClient, ChatRepository chatRepository) {
        simpleTelegramClient.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);
        this.telegramClient = simpleTelegramClient;
        this.chatRepository = chatRepository;
    }

}
