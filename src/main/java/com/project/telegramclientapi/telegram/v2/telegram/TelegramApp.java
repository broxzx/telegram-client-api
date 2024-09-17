package com.project.telegramclientapi.telegram.v2.telegram;

import com.project.telegramclientapi.telegram.v2.chat.model.Chat;
import com.project.telegramclientapi.telegram.v2.chat.repository.ChatRepository;
import com.project.telegramclientapi.telegram.v2.telegram.service.AuthenticationService;
import it.tdlight.client.Result;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
@RequiredArgsConstructor
public class TelegramApp {

    @Getter
    private final SimpleTelegramClient client;
    private final ChatRepository chatRepository;
    @Value("${telegram.adminId}")
    private long adminId;

    private void onUpdateHandler(TdApi.UpdateNewMessage incomingMessage) {
        TdApi.Message message = incomingMessage.message;
        TdApi.MessageContent messageContent = message.content;
        Chat chat = new Chat();

        fillWithCommonData(chat, message);

        List<byte[]> images = new ArrayList<>();
        List<String> pathToFiles = new ArrayList<>();

        if (message.senderId instanceof TdApi.MessageSenderUser messageSenderUser) {
            handleMessageSenderUserData(messageSenderUser, chat);
        }

        if (messageContent instanceof TdApi.MessageText messageText) {
            handleMessageTextData(messageText, chat);
        } else if (messageContent instanceof TdApi.MessagePhoto messagePhoto) {
            handleMessagePhotoData(messagePhoto, chat, pathToFiles, images);
        } else {
            chatRepository.save(chat);
        }
    }

    private void handleMessagePhotoData(TdApi.MessagePhoto messagePhoto, Chat chat, List<String> pathToFiles, List<byte[]> images) {
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

                    chatRepository.save(chat);
                    log.info("Chat saved successfully with images.");
                })
                .exceptionally(ex -> {
                    log.error("Error while downloading images or saving chat: {}", ex.getMessage());
                    return null;
                });
    }

    private CompletableFuture<Void> savePhotoToLocalStorage(List<String> pathToFiles, List<byte[]> images, TdApi.File remote) {
        return downloadPhotoAsync(remote)
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

    private void handleMessageTextData(TdApi.MessageText messageText, Chat chat) {
        TdApi.FormattedText formattedText = messageText.text;
        String text = formattedText.text;
        chat.setText(text); // text

        chatRepository.save(chat);
        System.out.println("Message text: " + text);
    }

    private static void handleMessageSenderUserData(TdApi.MessageSenderUser messageSenderUser, Chat chat) {
        chat.setSenderId(String.valueOf(messageSenderUser.userId)); // senderId
    }

    private CompletableFuture<String> downloadPhotoAsync(TdApi.File file) {
        CompletableFuture<String> future = new CompletableFuture<>();

        TdApi.DownloadFile downloadRequest = new TdApi.DownloadFile(file.id, 1, 0, 0, true);

        client.send(downloadRequest, fileResult -> {
            sendRequestGetPhotoTDApi(fileResult, future);
        });

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

    private void fillWithCommonData(Chat chat, TdApi.Message message) {
        chat.setChatId(String.valueOf(message.chatId));
        chat.setTime(message.date);
        chat.setRestData(message.toString());
    }

    @Autowired
    public TelegramApp(SimpleTelegramClientBuilder clientBuilder,
                       SimpleAuthenticationSupplier<?> authenticationData,
                       ChatRepository chatRepository,
                       AuthenticationService authenticationService) {
        this.chatRepository = chatRepository;
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, authenticationService::onUpdateAuthorizationState);
        clientBuilder.addCommandHandler("stop", this::onStopCommand);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);
        this.client = clientBuilder.build(authenticationData);
    }
}
