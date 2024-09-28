package com.company.group.services;

import com.company.group.components.GroupEntity;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.List;

public interface GroupService {
    void handle(Chat chat, Message message);

    void handleCallBackQuery(Long chatId, CallbackQuery callbackQuery);
    InlineKeyboardMarkup getInlineButtonForGroup();

    List<GroupEntity> getAll();
}
