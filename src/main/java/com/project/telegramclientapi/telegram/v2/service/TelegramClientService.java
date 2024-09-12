package com.project.telegramclientapi.telegram.v2.service;

import com.project.telegramclientapi.telegram.v2.config.TelegramConfig;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Getter
@Service
@Slf4j
public class TelegramClientService {

    private final SimpleTelegramClient client;

    public TelegramClientService(TelegramConfig config) {
        this.client = createTelegramClient(config);
    }

    private SimpleTelegramClient createTelegramClient(TelegramConfig config) {
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = config.getApiToken();
            TDLibSettings settings = TDLibSettings.create(apiToken);

            Path sessionPath = Paths.get("tdlib-session-id-fyuizee");
            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

            SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);
            return clientBuilder.build(AuthenticationSupplier.user(config.getPhoneNumber()));
        } catch (Exception e) {
            log.error("Error initializing Telegram client", e);
            throw new RuntimeException("Failed to initialize Telegram client", e);
        }
    }

    public void sendMessageToSavedMessages(long userId, String messageText) throws Exception {
        TdApi.Chat savedMessagesChat = client.send(new TdApi.CreatePrivateChat(userId, true)).get(1, TimeUnit.MINUTES);
        TdApi.SendMessage sendMessage = new TdApi.SendMessage();
        sendMessage.chatId = savedMessagesChat.id;

        TdApi.InputMessageText inputMessageText = new TdApi.InputMessageText();
        inputMessageText.text = new TdApi.FormattedText(messageText, new TdApi.TextEntity[0]);
        sendMessage.inputMessageContent = inputMessageText;

        TdApi.Message result = client.sendMessage(sendMessage, true).get(1, TimeUnit.MINUTES);
        log.info("sendMessageToSavedMessages: {}", result);
    }

}
