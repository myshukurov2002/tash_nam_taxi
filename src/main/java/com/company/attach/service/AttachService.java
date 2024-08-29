package com.company.attach.service;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface AttachService {
    void storing(Long chatId, Message photoSize);

    SendPhoto getSendPhoto(Long chatId);

    void deleteByChatId(Long chatId);
}
