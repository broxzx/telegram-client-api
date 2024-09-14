package com.project.telegramclientapi.telegram.v2.config;

import it.tdlight.client.APIToken;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.client.TDLibSettings;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Configuration
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
    @Qualifier("simpleTelegramClientBuilder-config")
    public SimpleTelegramClientBuilder simpleTelegramClientBuilder() {
        SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory();
        APIToken apiToken = getApiToken();
        TDLibSettings settings = TDLibSettings.create(apiToken);

        Path sessionPath = Paths.get("tdlib-session-id-fyuizee");
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));
        return clientFactory.builder(settings);
    }

    @Bean
    @Qualifier("adminId-config")
    public long adminId() {
        return adminId;
    }

    @Bean
    @Qualifier("phoneNumber-config")
    public String phoneNumber() {
        return phoneNumber;
    }

}
