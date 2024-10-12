package com.project.telegramclientapi.telegram;

import com.project.telegramclientapi.chat.model.Chat;
import it.tdlight.client.Result;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class TelegramMessageHandler {

    private final SimpleTelegramClient telegramClient;

    public void processMessagePhotoData(TdApi.MessageContent messageContent, Chat chat, List<String> pathToFiles, List<byte[]> images) {
        if (messageContent instanceof TdApi.MessagePhoto messagePhoto) {
            chat.setText(messagePhoto.caption.text);
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            Arrays.stream(messagePhoto.photo.sizes)
                    .forEach(photoSize -> {
                        TdApi.File remote = photoSize.photo;
                        CompletableFuture<Void> future = savePhotoToLocalStorage(pathToFiles, images, remote);

                        futures.add(future);
                    });

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        chat.setImages(images);
                        chat.setPathToFiles(pathToFiles);
                    })
                    .exceptionally(ex -> {
                        log.error("Error while downloading images or saving chat: {}", ex.getMessage());
                        return null;
                    });
        }
    }

    private CompletableFuture<Void> savePhotoToLocalStorage(List<String> pathToFiles, List<byte[]> images, TdApi.File remote) {
        return downloadPhotoAsync(remote, telegramClient)
                .thenAccept(result -> {
                    pathToFiles.add(result);
                    try {
                        byte[] fileContent = Files.readAllBytes(Path.of(result));
                        images.add(fileContent);
                    } catch (IOException exception) {
                        log.error("Error reading file: {}", exception.getMessage());
                    }
                });
    }

    public void processMessageTextData(TdApi.MessageContent messageContent, Chat chat) {
        if (messageContent instanceof TdApi.MessageText messageText) {
            TdApi.FormattedText formattedText = messageText.text;
            String text = formattedText.text;
            chat.setText(text);
        }
    }

    public void processMessageSenderUserData(TdApi.Message message, Chat chat) {
        if (message.senderId instanceof TdApi.MessageSenderUser messageSenderUser) {
            chat.setSenderId(String.valueOf(messageSenderUser.userId)); // senderId
        }
    }

    private CompletableFuture<String> downloadPhotoAsync(TdApi.File file, SimpleTelegramClient client) {
        CompletableFuture<String> future = new CompletableFuture<>();

        TdApi.DownloadFile downloadRequest = new TdApi.DownloadFile(file.id, 1, 0, 0, true);

        client.send(downloadRequest, fileResult -> sendRequestGetPhotoTDApi(fileResult, future));

        return future;
    }

    private static void sendRequestGetPhotoTDApi(Result<TdApi.File> fileResult, CompletableFuture<String> future) {
        TdApi.File downloadedFile = fileResult.get();

        if (downloadedFile.local.isDownloadingCompleted) {
            String localFilePath = downloadedFile.local.path;
            log.info("File uploaded: {}", localFilePath);
            future.complete(localFilePath);
        } else {
            log.info("File uploading has started...");
            future.completeExceptionally(new RuntimeException("Download not completed"));
        }
    }

    public void fillWithCommonData(Chat chat, TdApi.Message message) {
        chat.setChatId(String.valueOf(message.chatId));
        chat.setTime(message.date);
        chat.setRestData(message.toString());
        chat.setCreatedAt(LocalDateTime.now());
    }

}
