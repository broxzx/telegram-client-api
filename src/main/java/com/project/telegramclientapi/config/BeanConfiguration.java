package com.project.telegramclientapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfiguration {

    @Bean
    @Qualifier("objectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}
