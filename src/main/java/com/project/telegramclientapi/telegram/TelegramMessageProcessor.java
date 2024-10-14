package com.project.telegramclientapi.telegram;

import com.project.telegramclientapi.chat.model.Chat;
import it.tdlight.client.Result;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Data
public class TelegramMessageProcessor {

    private final SimpleTelegramClient telegramClient;

    private Chat chat;
    private List<byte[]> images;
    private List<String> pathToFiles;

    public CompletableFuture<Void> processMessagePhotoData(TdApi.MessageContent messageContent) {
        if (messageContent instanceof TdApi.MessagePhoto messagePhoto) {
            chat.setText(messagePhoto.caption.text);

            CompletableFuture<Void> processingFuture = processMessagePhoto(messagePhoto);

            return processingFuture.thenRun(() -> {
                chat.setImages(images);
                chat.setPathToFiles(pathToFiles);
                log.info("All images successfully processed and saved.");
            }).exceptionally(ex -> {
                log.error("Error while downloading images or saving chat: {}", ex.getMessage());
                return null;
            });
        }

        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> processMessagePhoto(TdApi.MessagePhoto messagePhoto) {
        CompletableFuture<Void> processingFuture = CompletableFuture.completedFuture(null);

        for (TdApi.PhotoSize photoSize : messagePhoto.photo.sizes) {
            TdApi.File remote = photoSize.photo;
            processingFuture = processingFuture.thenCompose(ignored -> savePhotoToLocalStorage(remote));
        }

        return processingFuture;
    }

    private CompletableFuture<Void> savePhotoToLocalStorage(TdApi.File remote) {
        return downloadPhotoAsync(remote, telegramClient)
                .thenAccept(result -> {
                    if (result != null) {
                        try {
                            Path sourcePath = Paths.get(result);
                            byte[] fileContent = Files.readAllBytes(sourcePath);
                            images.add(fileContent);
                            pathToFiles.add(result);

                            log.info("Image successfully downloaded and saved at {}", result);
                        } catch (IOException exception) {
                            log.error("Error reading file from path {}: {}", result, exception.getMessage());
                        }
                    } else {
                        log.error("File path is null, unable to save photo.");
                    }
                })
                .exceptionally(ex -> {
                    log.error("Error while saving photo to local storage: {}", ex.getMessage());
                    return null;
                });
    }

    public void processMessageTextData(TdApi.MessageContent messageContent) {
        if (messageContent instanceof TdApi.MessageText messageText) {
            TdApi.FormattedText formattedText = messageText.text;
            String text = formattedText.text;
            chat.setText(text);
        }
    }

    public void processMessageSenderUserData(TdApi.Message message) {
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

    private void sendRequestGetPhotoTDApi(Result<TdApi.File> fileResult, CompletableFuture<String> future) {
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

    public TelegramMessageProcessor(SimpleTelegramClient telegramClient, TdApi.UpdateNewMessage incomingMessage) {
        this.telegramClient = telegramClient;
        this.images = Collections.synchronizedList(new ArrayList<>());
        this.pathToFiles = Collections.synchronizedList(new ArrayList<>());
        this.chat = new Chat(incomingMessage.message);
    }

}
