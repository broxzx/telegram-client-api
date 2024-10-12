package com.project.telegramclientapi.config;

import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import jakarta.annotation.PreDestroy;
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
public class TelegramConfig {

    @Value("${telegram.apiId}")
    private int apiId;

    @Value("${telegram.apiHash}")
    private String apiHash;

    @Value("${telegram.phoneNumber}")
    private String phoneNumber;

    @Value("${telegram.adminId}")
    private long adminId;

    private final AuthenticationConfig authenticationConfig;
    private final AuthorizationStateHandler authorizationStateHandler;

    private SimpleTelegramClientFactory clientFactory;

    @Bean
    public SimpleTelegramClientBuilder adjustClient(TDLibSettings settings) {
        this.clientFactory = new SimpleTelegramClientFactory();
        SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);

        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, authorizationStateHandler::onUpdateAuthorizationState);

        return clientBuilder;
    }

    @Bean
    public SimpleTelegramClient simpleTelegramClientBuilder(SimpleTelegramClientBuilder clientBuilder) {
        return clientBuilder.build(authenticationConfig.simpleAuthenticationSupplier());
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

    @PreDestroy
    public void clientFactoryTearDown() {
        if (clientFactory != null) {
            log.info("closing client factory!");
            this.clientFactory.close();
        }
    }
}