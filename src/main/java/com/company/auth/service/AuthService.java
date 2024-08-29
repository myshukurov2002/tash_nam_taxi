package com.company.auth.service;

import com.company.auth.components.UserEntity;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface AuthService {
    void handleMessage(UserEntity user, Message message);

    UserEntity getUserById(Long chatId);

    void save(UserEntity user);

    void handleCallbackQuery(Long user, CallbackQuery callbackQuery);

    UserEntity getUserById(Long chatId, String firstName);
}
