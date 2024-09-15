package com.project.telegramclientapi.telegram.v2.telegram;

import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
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

    private void onUpdateHandler(TdApi.UpdateNewMessage incomingMessage) {
        TdApi.Message message = incomingMessage.message;
        TdApi.MessageContent messageContent = message.content;

        String textValue = extractText(messageContent.toString());

        log.info(incomingMessage.toString());
        System.out.println("textValue " + textValue);
    }

    public String extractText(String rawData) {
        Pattern pattern = Pattern.compile("text = \"([^\"]+)\"");
        Matcher matcher = pattern.matcher(rawData);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return null;
        }
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

    public TelegramApp(SimpleTelegramClientBuilder clientBuilder,
                       SimpleAuthenticationSupplier<?> authenticationData,
                       @Qualifier("adminId") long adminId) {
        this.adminId = adminId;
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addCommandHandler("stop", this::onStopCommand);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);
        this.client = clientBuilder.build(authenticationData);
    }

}
