package com.project.telegramclientapi;

import com.project.telegramclientapi.telegram.v2.service.TelegramAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
@Slf4j
public class TelegramClientApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(TelegramAppService telegramAppService) {
        return args -> {
            telegramAppService.initialize();
        };
    }

}
