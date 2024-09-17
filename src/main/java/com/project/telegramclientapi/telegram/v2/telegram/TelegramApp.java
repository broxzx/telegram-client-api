package com.project.telegramclientapi.telegram.v2.telegram;

import com.project.telegramclientapi.telegram.v2.chat.model.Chat;
import com.project.telegramclientapi.telegram.v2.chat.repository.ChatRepository;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class TelegramApp {

    @Getter
    private final SimpleTelegramClient client;
    private long adminId;
    private ChatRepository chatRepository;

    private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
        TdApi.AuthorizationState authorizationState = update.authorizationState;
        if (authorizationState instanceof TdApi.AuthorizationStateReady) {
            System.out.println("Logged in");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosing) {
            System.out.println("Closing...");
        } else if (authorizationState instanceof TdApi.AuthorizationStateClosed) {
            System.out.println("Closed");
        } else if (authorizationState instanceof TdApi.AuthorizationStateLoggingOut) {
            System.out.println("Logging out...");
        }
    }

    private void onUpdateHandler(TdApi.UpdateNewMessage incomingMessage) {
        TdApi.Message message = incomingMessage.message;
        TdApi.MessageContent messageContent = message.content;

        String senderId, chatId = String.valueOf(message.chatId), text = "";
        int time = message.date;
        List<byte[]> images = new ArrayList<>();
        List<String> pathToFiles = new ArrayList<>();

        if (message.senderId instanceof TdApi.MessageSenderUser messageSenderUser) {
            senderId = String.valueOf(messageSenderUser.userId);
        } else {
            senderId = "";
        }

        if (messageContent instanceof TdApi.MessageText messageText) {
            TdApi.FormattedText formattedText = messageText.text;
            text = formattedText.text;

            System.out.println("Message text: " + text);
        } else if (messageContent instanceof TdApi.MessagePhoto messagePhoto) {
            text = messagePhoto.caption.text;
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            Arrays.stream(messagePhoto.photo.sizes)
                    .forEach(photoSize -> {
                        TdApi.File remote = photoSize.photo;

                        CompletableFuture<Void> future = downloadPhotoAsync(remote)
                                .thenAccept(result -> {
                                    pathToFiles.add(result);
                                    try {
                                        byte[] fileContent = Files.readAllBytes(Path.of(result));
                                        images.add(fileContent);
                                    } catch (IOException exception) {
                                        log.error("Error reading file: {}", exception.getMessage());
                                    }
                                });

                        futures.add(future);
                    });

            String resultText = text;
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        Chat createdChat = Chat.builder()
                                .senderId(senderId)
                                .chatId(chatId)
                                .text(resultText)
                                .time(time)
                                .images(images)
                                .pathToFiles(pathToFiles)
                                .restData(message.toString())
                                .build();

                        chatRepository.save(createdChat);
                        log.info("Chat saved successfully with images.");
                    })
                    .exceptionally(ex -> {
                        log.error("Error while downloading images or saving chat: {}", ex.getMessage());
                        return null;
                    });
        } else {
            Chat createdChat = Chat.builder()
                    .senderId(senderId)
                    .chatId(chatId)
                    .text(text)
                    .time(time)
                    .restData(message.toString())
                    .build();

            chatRepository.save(createdChat);
        }
    }

    private CompletableFuture<String> downloadPhotoAsync(TdApi.File file) {
        CompletableFuture<String> future = new CompletableFuture<>();

        TdApi.DownloadFile downloadRequest = new TdApi.DownloadFile(file.id, 1, 0, 0, true);

        client.send(downloadRequest, fileResult -> {
            TdApi.File downloadedFile = fileResult.get();

            if (downloadedFile.local.isDownloadingCompleted) {
                String localFilePath = downloadedFile.local.path;
                log.info("File uploaded: {}", localFilePath);
                future.complete(localFilePath);
            } else {
                log.info("File uploading has started...");
                future.completeExceptionally(new RuntimeException("Download not completed"));
            }
        });

        return future;
    }

    private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
        if (isAdmin(commandSender)) {
            System.out.println("Received stop command. closing...");
            client.sendClose();
        }
    }

    public boolean isAdmin(TdApi.MessageSender sender) {
        if (sender instanceof TdApi.MessageSenderUser messageSenderUser) {
            return messageSenderUser.userId == adminId;
        } else {
            return false;
        }
    }

    public TelegramApp(SimpleTelegramClientBuilder clientBuilder,
                       SimpleAuthenticationSupplier<?> authenticationData) {
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addCommandHandler("stop", this::onStopCommand);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);
        this.client = clientBuilder.build(authenticationData);
    }

    @Autowired
    private void setAdminId(@Qualifier("adminId") int adminId) {
        this.adminId = adminId;
    }

    @Autowired
    private void setChatRepository(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }
}
