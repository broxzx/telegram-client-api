package com.project.telegramclientapi.telegram.v2.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "chat")
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class Chat {

    @Id
    private String id;

    private String senderId;

    private String chatId;

    private String text;

    private Integer time;

    private List<byte[]> images;

    private List<String> pathToFiles;

    private String restData;

}
