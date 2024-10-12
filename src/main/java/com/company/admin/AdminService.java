package com.company.admin;

import com.company.auth.components.UserEntity;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface AdminService {
    void handleMessage(UserEntity user, Message message);

    void handleCallBackQuery(UserEntity user, CallbackQuery callbackQuery);

    void init();
    void initCommands();
    void banTaxi(Long taxiId);
    public void getStatistics(Long chatId);
}
