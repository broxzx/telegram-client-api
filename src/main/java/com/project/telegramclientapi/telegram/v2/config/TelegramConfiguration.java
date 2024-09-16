package com.project.telegramclientapi.telegram.v2.config;

import it.tdlight.client.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Getter
public class TelegramConfiguration {

    @Value("${telegram.apiId}")
    private int apiId;

    @Value("${telegram.apiHash}")
    private String apiHash;

    @Value("${telegram.phoneNumber}")
    private String phoneNumber;

    @Value("${telegram.adminId}")
    private long adminId;

    public APIToken getApiToken() {
        return new APIToken(apiId, apiHash);
    }

    @Bean
    public SimpleTelegramClientBuilder adjustClient() {
        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            APIToken apiToken = getApiToken();
            TDLibSettings settings = TDLibSettings.create(apiToken);

            Path sessionPath = Paths.get("./src/main/resources/tdlib-session-id-fyuizee");
            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

            return clientFactory.builder(settings);
        }
    }

    @Bean
    public SimpleAuthenticationSupplier<?> simpleAuthenticationSupplier() {
        return AuthenticationSupplier.user(this.phoneNumber);
    }

    @Bean
    @Qualifier("adminId")
    public long adminId() {
        return this.adminId;
    }

    @Bean
    @Qualifier("phoneNumber")
    public String phoneNumber() {
        return this.phoneNumber;
    }
}
