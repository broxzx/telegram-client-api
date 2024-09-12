package com.project.telegramclientapi.telegram.v2.config;

import it.tdlight.client.APIToken;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TelegramConfig {

    @Value("${telegram.apiId}")
    private int apiId;

    @Value("${telegram.apiHash}")
    private String apiHash;

    @Getter
    @Value("${telegram.phoneNumber}")
    private String phoneNumber;

    public APIToken getApiToken() {
        return new APIToken(apiId, apiHash);
    }

}
