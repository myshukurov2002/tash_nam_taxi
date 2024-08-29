package com.company.attach.service;

import com.company.attach.AttachRepository;
import com.company.attach.components.AttachEntity;
import com.company.components.Components;
import com.company.expections.exp.AppBadRequestException;
import com.company.expections.exp.ItemNotFoundException;
import com.company.sender.SenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttachServiceImpl implements AttachService {

    @Value("${bot.token}")
    private String BOT_TOKEN;

    @Value("${photo.folder.name}")
    private String BASE_PATH;

    private final AttachRepository attachRepository;
    private final SenderService senderService;

    @Override
    public void storing(Long chatId, Message message) {

        try {
            PhotoSize photoSize = message
                    .getPhoto()
                    .stream()
                    .max(Comparator.comparingInt(PhotoSize::getFileSize))
                    .orElseThrow(() -> {
                        senderService.deleteMessage(chatId, message.getMessageId());
                        senderService.sendMessage(chatId, Components.INCORRECT_PHOTO);
                        return new RuntimeException();
                    });

            String uniqueId = UUID.randomUUID().toString();
            String originalFileName = "photo.jpg";
            String fileExtension = ".jpg";

            LocalDate currentDate = LocalDate.now();
            String directoryPath = String.format("%s/%d/%02d/%02d", BASE_PATH,
                    currentDate.getYear(),
                    currentDate.getMonthValue(),
                    currentDate.getDayOfMonth());
            java.io.File directory = new java.io.File(directoryPath);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String filePath = directoryPath + "/" + uniqueId + fileExtension;

            String fileId = photoSize.getFileId();
            if (fileId == null || fileId.isEmpty()) {
                throw new IllegalArgumentException("Invalid file ID from photoSize object");
            }

            GetFile getFile = new GetFile();
            getFile.setFileId(fileId);
            org.telegram.telegrambots.meta.api.objects.File file = senderService.initCommands(getFile);

            String fileUrl = String.format("https://api.telegram.org/file/bot%s/%s", BOT_TOKEN, file.getFilePath());
            log.info("Downloading file from: {}", fileUrl);

            try (InputStream in = new URL(fileUrl).openStream();
                 FileOutputStream out = new FileOutputStream(filePath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }

            AttachEntity attachEntity = AttachEntity.builder()
                    .id(uniqueId)
                    .chatId(chatId)
                    .originalName(originalFileName)
                    .path(filePath)
                    .size(new java.io.File(filePath).length())
                    .extension(fileExtension)
                    .build();

            attachRepository.save(attachEntity);

        } catch (Exception e) {
            senderService.deleteMessage(chatId, message.getMessageId());
            senderService.sendMessage(chatId, Components.WRONG_ANSWER);
            log.error("IOException occurred while downloading the file: ", e);
        }
    }

    @Override
    public SendPhoto getSendPhoto(Long chatId) {

        AttachEntity attach = findByChatId(chatId);
//        byte[] imageData = retrieveImage(attach);
//        File tempFile = new File("temp_" + attach.getId() + ".jpg");
//
//        try {
//            Files.write(tempFile.toPath(), imageData);
//            SendPhoto sendPhoto = new SendPhoto();
//            sendPhoto.setChatId(chatId);
//            sendPhoto.setPhoto(new InputFile(tempFile));
//
//            return sendPhoto;
//        } catch (IOException e) {
//            log.error(e.getMessage());
//            throw new RuntimeException(Components.ERROR_SEND_PHOTO, e);
//        } finally {
//            tempFile.delete();
//        }

        File file = new File(attach.getPath());
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(new InputFile(file));
        return sendPhoto;
    }

    @Override
    public void deleteByChatId(Long chatId) {
        AttachEntity entity = get(chatId);
        try {
            String url = entity.getPath();
            File file = new File(url);
            if (file.exists()) {
                if (file.delete()) {
                    attachRepository.delete(entity);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new AppBadRequestException();
        }
    }

    private AttachEntity get(Long chatId) {
        return attachRepository
                .findByChatIdAndVisibilityTrue(chatId)
                .orElseThrow();
    }

    private AttachEntity findByChatId(Long chatId) {
        return attachRepository
                .findByChatIdAndVisibilityTrue(chatId)
                .orElseThrow(ItemNotFoundException::new);
    }

    private byte[] retrieveImage(AttachEntity attachEntity) {

        Path path = Paths.get(attachEntity.getPath());
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(Components.ERROR_READING_IMAGE, e);
        }
    }

}
