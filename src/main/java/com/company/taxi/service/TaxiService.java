package com.company.taxi.service;

import com.company.auth.components.UserEntity;
import com.company.client.components.VoyageEntity;
import com.company.taxi.components.TaxiEntity;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public interface TaxiService {

    void handleMessage(UserEntity user, Message message);

    void handleCallbackQuery(UserEntity user, CallbackQuery callbackQuery);

    TaxiEntity getById(Long taxiId);

    void updateStatus(TaxiEntity taxi);

    void deleteByChatId(TaxiEntity taxi);

    void sendVoyage(VoyageEntity voyage, Integer messageId);

    List<TaxiEntity> findAll();

    void save(TaxiEntity taxi);

    void getInfo(TaxiEntity taxi, Long chatId);

    boolean existsById(Long chatId);
}
