package com.project.telegramclientapi.config;

import com.project.telegramclientapi.chat.repository.ChatRepository;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Getter
@RequiredArgsConstructor
@Slf4j
public class TelegramConfiguration {

    @Value("${telegram.apiId}")
    private int apiId;

    @Value("${telegram.apiHash}")
    private String apiHash;

    @Value("${telegram.phoneNumber}")
    private String phoneNumber;

    @Value("${telegram.adminId}")
    private long adminId;

    private final ChatRepository chatRepository;
    private final AuthenticationConfig authenticationConfig;

    private SimpleTelegramClient telegramClient;


    @Bean
    public SimpleTelegramClientBuilder adjustClient(TDLibSettings settings) {
        SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory();

        SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);

        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);

        return clientBuilder;
    }

    @Bean
    public SimpleTelegramClient simpleTelegramClientBuilder(SimpleTelegramClientBuilder clientBuilder) {
        telegramClient = clientBuilder.build(authenticationConfig.simpleAuthenticationSupplier());
        return telegramClient;
    }


    @Bean
    public TDLibSettings tdLibSettings() {
        APIToken apiToken = new APIToken(apiId, apiHash);
        TDLibSettings settings = TDLibSettings.create(apiToken);

        Path sessionPath = Paths.get("./src/main/resources/tdlib-session-id-fyuizee");
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

        return settings;
    }


    public void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
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
}