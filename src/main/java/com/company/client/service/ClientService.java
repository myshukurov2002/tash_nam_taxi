package com.company.client.service;

import com.company.auth.components.UserEntity;
import com.company.client.components.ClientEntity;
import com.company.client.components.VoyageEntity;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface ClientService {
    void handleMessage(UserEntity user, Message message);

    void handleCallbackQuery(UserEntity user, CallbackQuery callbackQuery);
    VoyageEntity getVoyage(Long voyageId);

    void deleteByChatId(Long chatId);

    void getAllVoyages(Long chatId);

    List<ClientEntity> getAll();

    List<VoyageEntity> getAllVoyages();
}
