package com.project.telegramclientapi.telegram.v2.service;

import com.project.telegramclientapi.telegram.v2.config.TelegramConfiguration;
import com.project.telegramclientapi.telegram.v2.utils.TelegramApp;
import it.tdlight.client.APIToken;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
public class TelegramClientService {

    private final TelegramConfiguration telegramConfiguration;

    @SneakyThrows
    public void adjustTelegramClient(long adminId) {
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            SimpleTelegramClientBuilder clientBuilder = adjustClient(clientFactory);
            TelegramApp app = new TelegramApp(clientBuilder, adminId, telegramConfiguration.getPhoneNumber());

            TdApi.User me = app.getClient().getMeAsync().get(1, TimeUnit.MINUTES);
            sendMessageToFavourite(me, app);
        }
    }

    private SimpleTelegramClientBuilder adjustClient(SimpleTelegramClientFactory clientFactory) {
        APIToken apiToken = telegramConfiguration.getApiToken();
        TDLibSettings settings = TDLibSettings.create(apiToken);

        Path sessionPath = Paths.get("tdlib-session-id-fyuizee");
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

        return clientFactory.builder(settings);
    }

    private void sendMessageToFavourite(TdApi.User me, TelegramApp app) throws InterruptedException, ExecutionException, TimeoutException {
        TdApi.Chat savedMessagesChat = app.getClient().send(new TdApi.CreatePrivateChat(me.id, true)).get(1, TimeUnit.MINUTES);
        TdApi.SendMessage req = new TdApi.SendMessage();
        req.chatId = savedMessagesChat.id;
        TdApi.InputMessageText txt = new TdApi.InputMessageText();
        txt.text = new TdApi.FormattedText("TDLight test", new TdApi.TextEntity[0]);
        req.inputMessageContent = txt;
        TdApi.Message result = app.getClient().sendMessage(req, true).get(1, TimeUnit.MINUTES);
        System.out.println("Sent message:" + result);
    }

}
