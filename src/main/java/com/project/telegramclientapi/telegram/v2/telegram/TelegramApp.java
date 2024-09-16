package com.project.telegramclientapi.telegram.v2.telegram;

import com.project.telegramclientapi.telegram.v2.chat.model.Chat;
import com.project.telegramclientapi.telegram.v2.chat.repository.ChatRepository;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.jni.TdApi;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Component
public class TelegramApp {

    @Getter
    private final SimpleTelegramClient client;
    private final long adminId;
    private final ChatRepository chatRepository;

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

        String senderId = "", chatId = String.valueOf(message.chatId), text = "";
        int time = message.date;
        List<byte[]> images = new ArrayList<>();
        List<String> pathToFiles = new ArrayList<>();


        if (message.senderId instanceof TdApi.MessageSenderUser messageSenderUser) {
            senderId = String.valueOf(messageSenderUser.userId);
        }

        if (messageContent instanceof TdApi.MessageText messageText) {
            TdApi.FormattedText formattedText = messageText.text;
            text = formattedText.text;

            System.out.println("Message text: " + text);
        } else if (messageContent instanceof TdApi.MessagePhoto messagePhoto) {
            Arrays.stream(messagePhoto.photo.sizes)
                    .forEach(photoSize -> {
                        TdApi.File remote = photoSize.photo;

                        downloadPhoto(remote, localFilePath -> {
                            try {
                                File file = new File(localFilePath);
                                byte[] fileContent = Files.readAllBytes(file.toPath());
                                images.add(fileContent);
                                log.info("File content added to images list.");
                            } catch (IOException exception) {
                                log.error("Error reading file: {}", exception.getMessage());
                            }
                        });
                    });
        }

        log.info("images: {}", images);
        Chat createdChat = Chat.builder()
                .senderId(senderId)
                .chatId(chatId)
                .text(text)
                .time(time)
                .images(images)
                .pathToFiles(pathToFiles)
                .restData(message.toString())
                .build();

        chatRepository.save(createdChat);
    }

    private void downloadPhoto(TdApi.File file, Consumer<String> onDownloadComplete) {
        TdApi.DownloadFile downloadRequest = new TdApi.DownloadFile(file.id, 1, 0, 0, true);

        client.send(downloadRequest, fileResult -> {
            TdApi.File downloadedFile = fileResult.get();

            if (downloadedFile.local.isDownloadingCompleted) {
                String localFilePath = downloadedFile.local.path;
                log.info("File uploaded: {}", localFilePath);
                onDownloadComplete.accept(localFilePath);
            } else {
                log.info("File uploading has started...");
            }
        });
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
                       SimpleAuthenticationSupplier<?> authenticationData,
                       @Qualifier("adminId") long adminId, ChatRepository chatRepository) {
        this.adminId = adminId;
        clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);
        clientBuilder.addCommandHandler("stop", this::onStopCommand);
        clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);
        this.client = clientBuilder.build(authenticationData);
        this.chatRepository = chatRepository;
    }

}
