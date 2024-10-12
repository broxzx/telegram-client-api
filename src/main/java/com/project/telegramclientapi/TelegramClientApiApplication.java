package com.project.telegramclientapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramClientApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApiApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner commandLineRunner(TelegramAppService telegramAppService) {
//        return args -> {
//            telegramAppService.initialize();
//        };
//    }

}
