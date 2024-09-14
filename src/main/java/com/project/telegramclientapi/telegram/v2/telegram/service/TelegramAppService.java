package com.project.telegramclientapi.telegram.v2.telegram.service;

import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.util.UnsupportedNativeLibraryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramAppService {

    private final TelegramClientService telegramClientService;

    public void initialize() {
        long adminId = Integer.getInteger("it.tdlight.example.adminid", 667900586);
        initializedNativeClasses();
        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());

        telegramClientService.adjustTelegramClient(adminId);
    }

    private void initializedNativeClasses() {
        try {
            Init.init();
        } catch (UnsupportedNativeLibraryException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
