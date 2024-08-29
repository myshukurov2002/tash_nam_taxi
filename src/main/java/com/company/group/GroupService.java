package com.company.group;

import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface GroupService {
    void handle(Chat chat, Message message);

    void handleCallBackQuery(Long chatId, CallbackQuery callbackQuery);
}
