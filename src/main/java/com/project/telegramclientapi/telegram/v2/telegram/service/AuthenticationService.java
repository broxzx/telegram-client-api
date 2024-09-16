package com.project.telegramclientapi.telegram.v2.telegram.service;

import com.project.telegramclientapi.telegram.v2.chat.repository.ChatRepository;
import com.project.telegramclientapi.telegram.v2.telegram.TelegramApp;
import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClientBuilder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationService {

    public static TelegramApp initiateTelegramApp(SimpleTelegramClientBuilder clientBuilder,
                                                  String phoneNumber,
                                                  long adminId,
                                                  ChatRepository chatRepository) {
        SimpleAuthenticationSupplier<?> authenticationData = AuthenticationSupplier.user(phoneNumber);
        return new TelegramApp(clientBuilder, authenticationData, adminId, chatRepository);
    }

}
