package com.company;

import com.company.admin.AdminService;
import com.company.auth.components.UserEntity;
import com.company.auth.components.UserRole;
import com.company.auth.components.UserState;
import com.company.auth.service.AuthService;
import com.company.client.components.VoyageEntity;
import com.company.client.service.ClientService;
import com.company.components.Components;
import com.company.group.services.GroupService;
import com.company.sender.SenderService;
import com.company.taxi.service.TaxiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

@Component
@RequiredArgsConstructor
public class MessageHandler {

    private final ClientService clientService;
    private final TaxiService taxiService;
    private final SenderService senderService;
    private final AuthService authService;
    private final AdminService adminService;
    private final GroupService groupService;

    public void handleMessage(Message message) {

        Chat chat = message.getChat();
        Long chatId = chat.getId();
        String firstName = chat.getFirstName();
        UserEntity user = authService.getUserById(chatId, firstName);
        UserRole userRole = user.getUserRole();

        if (user.getUserState().equals(UserState.BANNED)) {
            senderService.sendMessage(chatId, "¯\u2060\\\u2060_\u2060(\u2060ツ\u2060)\u2060_\u2060/\u2060¯");
            return;
        }

        if (message.getFrom().getUserName() != null &&
            user.getUsername() == null) {
            user.setUsername(message.getFrom().getUserName());
            authService.save(user);
        }

        if (message.isCommand() &&
             user.getUserState().equals(UserState.USER_TYPE) ) {
            commandService(user, message);
            return;
        }

        switch (userRole) {
            case CLIENT -> clientService.handleMessage(user, message);
            case TAXIST -> taxiService.handleMessage(user, message);
            case ADMIN, SUPER_ADMIN -> adminService.handleMessage(user, message);
            default -> authService.handleMessage(user, message);
        }
    }

    public void handleCallbackQuery(Long chatId, CallbackQuery callbackQuery) {

        UserEntity user = authService.getUserById(chatId);
        UserRole userRole = user.getUserRole();

        if (callbackQuery.getData().startsWith(Components.ILL_GET)) {

            groupService.handleCallBackQuery(chatId, callbackQuery);
            return;
        }

        switch (userRole) {
            case CLIENT -> clientService.handleCallbackQuery(user, callbackQuery);
            case TAXIST -> taxiService.handleCallbackQuery(user, callbackQuery);
            case ADMIN, SUPER_ADMIN -> adminService.handleCallBackQuery(user, callbackQuery);
            default -> authService.handleCallbackQuery(chatId, callbackQuery);
        }
    }

    public void handleVoyage(VoyageEntity voyage, Integer messageId) {
        taxiService.sendVoyage(voyage, messageId);
    }

    private void commandService(UserEntity user, Message message) {
        Long chatId = user.getChatId();
        String command = message.getText();

        if (!user.getUserState().equals(UserState.USER_TYPE)) {
            senderService.sendMessage(user.getChatId(), Components.SHOULD_FILL);
        }
        switch (command) {
//            case "/start": {
//                getMenu(user, message, chatId);
//            }
            case "/statistics": {
                getMenu(user, message, chatId);
            }
            case "/help": {
                Chat chat = message.getChat();
                String firstName = chat.getFirstName();
                String userName = chat.getUserName();
                user.setFullName(firstName);
                user.setUsername(userName);
                user.setUserState(UserState.USER_TYPE);
                authService.save(user);
                senderService.sendMessage(chatId, Components.HELP);
                senderService.sendMessage(chatId, Components.HELP_2, senderService.getGroupUrl());
            }
            case "/menu": {
                if (user.getUserState().equals(UserState.USER_TYPE) ||
                    user.getUserState().equals(UserState.REGISTRATION_DONE))
                    senderService.askUserType(chatId, Components.MAIN_MENU);
            }
        }
    }

    private void getMenu(UserEntity user, Message message, Long chatId) {
        switch (user.getUserRole()) {
            case TAXIST -> taxiService.handleMessage(user, message);
            case CLIENT -> clientService.handleMessage(user, message);
            default -> senderService.sendMessage(chatId, Components.NOT_AVAILABLE);
        }
    }

}
