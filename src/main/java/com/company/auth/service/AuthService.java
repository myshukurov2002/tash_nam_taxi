package com.company.auth.service;

import com.company.auth.components.UserEntity;
import jdk.dynalink.linker.LinkerServices;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface AuthService {
    void handleMessage(UserEntity user, Message message);

    UserEntity getUserById(Long chatId);

    void save(UserEntity user);

    void handleCallbackQuery(Long user, CallbackQuery callbackQuery);

    UserEntity getUserById(Long chatId, String firstName);

    List<UserEntity> getAll();

    void deleteByChatId(Long chatId);

    void create(Long userId, String role);

    int count();
}
