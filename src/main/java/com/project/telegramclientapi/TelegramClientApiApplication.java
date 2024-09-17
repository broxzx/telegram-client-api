package com.project.telegramclientapi;

import com.project.telegramclientapi.telegram.v2.chat.repository.ChatRepository;
import com.project.telegramclientapi.telegram.v2.telegram.service.TelegramAppService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@SpringBootApplication
@Slf4j
@RestController
@RequestMapping("/pictures")
@RequiredArgsConstructor
public class TelegramClientApiApplication {

    private final ChatRepository chatRepository;

    public static void main(String[] args) {
        SpringApplication.run(TelegramClientApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(TelegramAppService telegramAppService) {
        return args -> {
            telegramAppService.initialize();
        };
    }

    @GetMapping
    public ResponseEntity<byte[]> displayImage() {
        List<byte[]> image = Objects.requireNonNull(chatRepository.findAll()
                        .stream()
                        .filter(chat -> chat != null && chat.getImages() != null && !chat.getImages().isEmpty())
                        .findFirst()
                        .orElse(null))
                .getImages();

        log.info("{}", image);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);

        return new ResponseEntity<>(image.get(0), headers, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<byte[]> uploadPicture(@ModelAttribute MultipartFile picture) {
        try {
            byte[] image = picture.getBytes();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.IMAGE_PNG);
            return new ResponseEntity<>(image, httpHeaders, HttpStatus.OK);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
