package com.project.telegramclientapi.telegram.v2.chat.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chat")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Chat {

    @Id
    private String id;

    private String senderId;

    private String chatId;

    private String text;

    private Long time;

    private String restData;

}
