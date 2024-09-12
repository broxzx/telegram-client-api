package com.project.telegramclientapi.telegram.v1;

import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.*;
import it.tdlight.util.UnsupportedNativeLibraryException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
@Slf4j
public final class Telegram {

    @Value("${telegram.apiId}")
    private int apiId;
    @Value("${telegram.apiHash}")
    private String apiHash;
    @Value("${telegram.phoneNumber}")
    private String phoneNumber;

    public void init() throws Exception {
        long adminId = Integer.getInteger("it.tdlight.example.adminid", 667900586);
        initializedNativeClasses();

        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());

        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            SimpleTelegramClientBuilder clientBuilder = adjustTelegramClient(clientFactory);
            TelegramApp app = authenticateViaToken(clientBuilder, adminId);

            TdApi.User me = app.getClient().getMeAsync().get(1, TimeUnit.MINUTES);
            sendMessageToFavourite(me, app);

        }
    }

    private static void sendMessageToFavourite(TdApi.User me, TelegramApp app) throws InterruptedException, ExecutionException, TimeoutException {
        Chat savedMessagesChat = app.getClient().send(new CreatePrivateChat(me.id, true)).get(1, TimeUnit.MINUTES);
        SendMessage req = new SendMessage();
        req.chatId = savedMessagesChat.id;
        InputMessageText txt = new InputMessageText();
        txt.text = new FormattedText("TDLight test", new TextEntity[0]);
        req.inputMessageContent = txt;
        Message result = app.getClient().sendMessage(req, true).get(1, TimeUnit.MINUTES);
        System.out.println("Sent message:" + result);
    }

    private @NotNull TelegramApp authenticateViaToken(SimpleTelegramClientBuilder clientBuilder, long adminId) {
        SimpleAuthenticationSupplier<?> authenticationData = AuthenticationSupplier.user(phoneNumber);

        return new TelegramApp(clientBuilder, authenticationData, adminId);
    }

    private SimpleTelegramClientBuilder adjustTelegramClient(SimpleTelegramClientFactory clientFactory) {
        APIToken apiToken = new APIToken(apiId, apiHash);

        TDLibSettings settings = TDLibSettings.create(apiToken);

        Path sessionPath = Paths.get("tdlib-session-id-fyuizee");
        settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
        settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

        return clientFactory.builder(settings);
    }

    private void initializedNativeClasses() {
        try {
            Init.init();
        } catch (UnsupportedNativeLibraryException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public static class TelegramApp {

        @Getter
        private final SimpleTelegramClient client;

        private final long adminId;

        public TelegramApp(SimpleTelegramClientBuilder clientBuilder,
                           SimpleAuthenticationSupplier<?> authenticationData,
                           long adminId) {
            this.adminId = adminId;

            clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);

            clientBuilder.addCommandHandler("stop", this::onStopCommand);

            clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);

            this.client = clientBuilder.build(authenticationData);
        }

        private void onUpdateAuthorizationState(TdApi.UpdateAuthorizationState update) {
            AuthorizationState authorizationState = update.authorizationState;
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

        private void onUpdateHandler(TdApi.Object object) {
            TdApi.Update update = (TdApi.Update) object;
            log.info(update.toString());
        }

        private void onStopCommand(TdApi.Chat chat, TdApi.MessageSender commandSender, String arguments) {
            if (isAdmin(commandSender)) {
                System.out.println("Received stop command. closing...");
                client.sendClose();
            }
        }

        public boolean isAdmin(TdApi.MessageSender sender) {
            if (sender instanceof MessageSenderUser messageSenderUser) {
                return messageSenderUser.userId == adminId;
            } else {
                return false;
            }
        }

    }
}