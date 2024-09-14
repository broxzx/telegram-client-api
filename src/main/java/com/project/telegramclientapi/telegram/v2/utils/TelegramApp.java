package com.project.telegramclientapi.telegram.v2.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.jni.TdApi.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TelegramApp {

    @Getter
    private final SimpleTelegramClient client;
    private ObjectMapper objectMapper;

    private final long adminId;

    private void onUpdateAuthorizationState(UpdateAuthorizationState update) {
        AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof AuthorizationStateReady) {
            System.out.println("Logged in");
        } else if (authorizationState instanceof AuthorizationStateClosing) {
            System.out.println("Closing...");
        } else if (authorizationState instanceof AuthorizationStateClosed) {
            System.out.println("Closed");
        } else if (authorizationState instanceof AuthorizationStateLoggingOut) {
            System.out.println("Logging out...");
        }
    }

    private void onUpdateHandler(UpdateNewMessage incomingMessage) {
        Message obtainedMessage = incomingMessage.message;
        MessageContent content = obtainedMessage.content;

        try {
            String s = objectMapper.writeValueAsString(content);
            log.info("test: {}", s);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info(incomingMessage.toString());
    }

    private void onStopCommand(Chat chat, MessageSender commandSender, String arguments) {
        if (isAdmin(commandSender)) {
            System.out.println("Received stop command. closing...");
            client.sendClose();
        }
    }

    public boolean isAdmin(MessageSender sender) {
        if (sender instanceof MessageSenderUser messageSenderUser) {
            return messageSenderUser.userId == adminId;
        } else {
            return false;
        }
    }

    public TelegramApp(@Qualifier("simpleTelegramClientBuilder-config") SimpleTelegramClientBuilder clientBuilder,
                       @Qualifier("adminId-config") long adminId,
                       @Qualifier("phoneNumber-config") String phoneNumber) {
        SimpleAuthenticationSupplier<?> authenticationData = AuthenticationSupplier.user(phoneNumber);
        this.adminId = adminId;
        clientBuilder.addUpdateHandler(UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addCommandHandler("stop", this::onStopCommand);
        clientBuilder.addUpdateHandler(UpdateNewMessage.class, this::onUpdateHandler);
        this.client = clientBuilder.build(authenticationData);
    }

    @Autowired
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
