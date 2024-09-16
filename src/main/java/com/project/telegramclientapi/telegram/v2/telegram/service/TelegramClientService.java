package com.project.telegramclientapi.telegram.v2.telegram.service;

import com.project.telegramclientapi.telegram.v2.chat.repository.ChatRepository;
import com.project.telegramclientapi.telegram.v2.config.TelegramConfiguration;
import com.project.telegramclientapi.telegram.v2.telegram.TelegramApp;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramClientService {

    private final TelegramConfiguration telegramConfiguration;
    private final SimpleTelegramClientBuilder clientBuilder;
    private final ChatRepository chatRepository;

    public void adjustTelegramClient(long adminId) {
        try (SimpleTelegramClientFactory ignored = new SimpleTelegramClientFactory()) {
            TelegramApp app = AuthenticationService.initiateTelegramApp(clientBuilder, telegramConfiguration.getPhoneNumber(), adminId, chatRepository);

            try {
                TdApi.User me = app.getClient().getMeAsync().get(1, TimeUnit.MINUTES);
                sendMessageToFavourite(me, app);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
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
