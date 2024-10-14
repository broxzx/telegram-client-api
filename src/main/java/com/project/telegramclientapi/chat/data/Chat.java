package com.project.telegramclientapi.chat.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import it.tdlight.jni.TdApi;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
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

    private List<String> pathToFiles;

    @JsonIgnore
    private String restData;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    public Chat(TdApi.Message message) {
        this.chatId = String.valueOf(message.chatId);
        this.time = message.date;
        this.restData = message.toString();
        this.createdAt = LocalDateTime.now();
    }

}
