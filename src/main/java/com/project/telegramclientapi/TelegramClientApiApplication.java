package com.project.telegramclientapi;

import com.project.telegramclientapi.telegram.Example;
import com.project.telegramclientapi.telegram.Telegram;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class TelegramClientApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(Example telegram) {
        return args -> {
              telegram.init();
        };
    }

}
