package com.project.telegramclientapi.telegram.v2.utils;

import com.project.telegramclientapi.telegram.v2.service.TelegramAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BotInitializer {

    private final TelegramAppService telegramAppService;

    public void run() {
        try {
            telegramAppService.run();
        } catch (Exception e) {
            log.error("Error running Telegram bot", e);
            throw new RuntimeException(e);
        }
    }

}
