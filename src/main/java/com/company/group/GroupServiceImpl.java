package com.company.group;

import com.company.components.Components;
import com.company.sender.SenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    @Value("${bot.username}")
    private String GROUP_LINK;

    @Value("${bot.url}")
    private String BOT_URL;

    @Value("${taxi.group.id}")
    private Long TAXI_GROUP_ID;

    private final SenderService senderService;

    @Override
    public void handle(Chat chat, Message message) {

        Long chatId = chat.getId();
        System.out.println(chat.getId());

        if (chatId.equals(TAXI_GROUP_ID)) {
            return;
        }

        if(message.isCommand()) {
            String text = message.getText();
            if (text.startsWith("/help")) {
                senderService.sendMessage(chatId, Components.HELP, senderService.getGroupUrl());
                return;
            } else {
                senderService.deleteMessage(chatId, message.getMessageId());
            }
        }

        if (!isUserAdmin(chatId, message.getFrom().getId())) {
            senderService.deleteMessage(chatId, message.getMessageId());
            senderService.sendMessage(chatId, Components.GROUP_ADS + "\n" + GROUP_LINK, getInlineButtonForGroup());
        }
    }

    private InlineKeyboardMarkup getInlineButtonForGroup() {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton inlineKeyboardButton = InlineKeyboardButton
                .builder()
                .text(Components.GROUP_LINK)
                .url(BOT_URL)
                .build();

        row1.add(inlineKeyboardButton);
        rows.add(row1);
        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    private boolean isUserAdmin(long chatId, long userId) {

        GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
        getChatAdministrators.setChatId(chatId);

        List<ChatMember> administrators = senderService.getAdmins(getChatAdministrators);
        for (ChatMember admin : administrators) {
            if (admin.getUser().getId() == userId) {
                return true;
            }
        }
        return false;
    }
}
