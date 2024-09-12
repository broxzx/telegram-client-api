package com.project.telegramclientapi.telegram;

import it.tdlight.*;
import it.tdlight.client.*;
import it.tdlight.jni.TdApi;
import it.tdlight.jni.TdApi.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public final class Example {

    @Value("${telegram.apiId}")
    private int apiId;
    @Value("${telegram.apiHash}")
    private String apiHash;
    @Value("${telegram.phoneNumber}")
    private String phoneNumber;

    public void init() throws Exception {
        long adminId = Integer.getInteger("it.tdlight.example.adminid", 667900586);
        Init.init();

        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());

        try (SimpleTelegramClientFactory clientFactory = new SimpleTelegramClientFactory()) {
            var apiToken = new APIToken(apiId, apiHash);

            TDLibSettings settings = TDLibSettings.create(apiToken);

            Path sessionPath = Paths.get("tdlib-session-id-fyuizee");
            settings.setDatabaseDirectoryPath(sessionPath.resolve("data"));
            settings.setDownloadedFilesDirectoryPath(sessionPath.resolve("downloads"));

            SimpleTelegramClientBuilder clientBuilder = clientFactory.builder(settings);

            SimpleAuthenticationSupplier<?> authenticationData = AuthenticationSupplier.user(phoneNumber);

            var app = new ExampleApp(clientBuilder, authenticationData, adminId);
            TdApi.User me = app.getClient().getMeAsync().get(1, TimeUnit.MINUTES);

            var savedMessagesChat = app.getClient().send(new CreatePrivateChat(me.id, true)).get(1, TimeUnit.MINUTES);

            var req = new SendMessage();
            req.chatId = savedMessagesChat.id;
            var txt = new InputMessageText();
            txt.text = new FormattedText("TDLight test", new TextEntity[0]);
            req.inputMessageContent = txt;
            Message result = app.getClient().sendMessage(req, true).get(1, TimeUnit.MINUTES);
            System.out.println("Sent message:" + result);

            log.info(me.toString());

            log.info("stop here");

        }
    }

    public static class ExampleApp implements AutoCloseable {

        private final SimpleTelegramClient client;

        private final long adminId;

        public ExampleApp(SimpleTelegramClientBuilder clientBuilder,
                          SimpleAuthenticationSupplier<?> authenticationData,
                          long adminId) {
            this.adminId = adminId;

            clientBuilder.addUpdateHandler(TdApi.UpdateAuthorizationState.class, this::onUpdateAuthorizationState);

            clientBuilder.addCommandHandler("stop", this::onStopCommand);

            clientBuilder.addUpdateHandler(TdApi.UpdateNewMessage.class, this::onUpdateHandler);

            this.client = clientBuilder.build(authenticationData);
        }

        @Override
        public void close() throws Exception {
            client.close();
        }

        public SimpleTelegramClient getClient() {
            return client;
        }

        /**
         * Print the bot status
         */
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

        private void onUpdateNewMessage(TdApi.UpdateNewMessage update) {
            MessageContent messageContent = update.message.content;

            String text;
            if (messageContent instanceof TdApi.MessageText messageText) {
                text = messageText.text.text;
            } else {
                text = String.format("(%s)", messageContent.getClass().getSimpleName());
            }

            long chatId = update.message.chatId;

            client.send(new TdApi.GetChat(chatId))
                    .whenCompleteAsync((chatIdResult, error) -> {
                        if (error != null) {
                            System.err.printf("Can't get chat title of chat %s%n", chatId);
                            error.printStackTrace(System.err);
                        } else {
                            String title = chatIdResult.title;
                            System.out.printf("Received new message from chat %s (%s): %s%n", title, chatId, text);
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
            if (sender instanceof MessageSenderUser messageSenderUser) {
                return messageSenderUser.userId == adminId;
            } else {
                return false;
            }
        }

    }
}