package com.project.telegramclientapi.telegram.v2.utils;

import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TelegramApp {

    @Getter
    private final SimpleTelegramClient client;

    private final long adminId;

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            System.out.println("Logged in");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            System.out.println("Closing...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            System.out.println("Closed");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            System.out.println("Logging out...");
        }
    }

    private void onUpdateHandler(TdApi.Object object) {
        TdApi.Update update = (TdApi.Update) object;
        log.info(update.toString());
    }

    private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
        if (isAdmin(commandSender)) {
            System.out.println("Received stop command. closing...");
            client.sendClose();
        }
    }

    public boolean isAdmin(TdApi.MessageSender sender) {
        if (sender instanceof TdApi.MessageSenderUser messageSenderUser) {
            return messageSenderUser.userId == adminId;
        } else {
            return false;
        }
    }

    public TelegramApp(SimpleTelegramClientBuilder clientBuilder, long adminId, String phoneNumber) {
        SimpleAuthenticationSupplier<?> authenticationData = AuthenticationSupplier.user(phoneNumber);
        this.adminId = adminId;
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addCommandHandler("stop", this::onStopCommand);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);
        this.client = clientBuilder.build(authenticationData);
    }


}
