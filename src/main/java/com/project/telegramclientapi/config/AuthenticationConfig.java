package com.project.telegramclientapi.config;

import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleAuthenticationSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@RequiredArgsConstructor
@Slf4j
public class AuthenticationConfig {

    @Value("${telegram.phoneNumber}")
    private String phoneNumber;

    @Bean
    public SimpleAuthenticationSupplier<?> simpleAuthenticationSupplier() {
        return AuthenticationSupplier.user(this.phoneNumber);
    }

}
