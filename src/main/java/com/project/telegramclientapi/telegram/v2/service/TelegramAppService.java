package com.project.telegramclientapi.telegram.v2.service;

import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.jni.TdApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class TelegramAppService {

    private final TelegramClientService clientService;

    @Value("${telegram.adminId}")
    private long adminId;

    public TelegramAppService(TelegramClientService clientService) {
        this.clientService = clientService;
    }

    public void run() throws Exception {
        TdApi.User currentUser = clientService.getClient().getMeAsync().get(1, TimeUnit.MINUTES);
        log.info("Logged in as: {}", currentUser.firstName);

        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());

        clientService.sendMessageToSavedMessages(currentUser.id, "Hello from Telegram Bot!");
    }

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                log.info("Authorization successful!");
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                log.info("Logging out...");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                log.info("Session closed.");
                break;
            default:
                log.info("Unhandled state: {}", authorizationState);
        }
    }

}
