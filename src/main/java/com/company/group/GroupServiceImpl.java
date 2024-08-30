package com.company.group;

import com.company.auth.components.UserEntity;
import com.company.auth.service.AuthService;
import com.company.client.components.VoyageEntity;
import com.company.client.service.ClientService;
import com.company.components.Components;
import com.company.sender.SenderService;
import com.company.taxi.components.TaxiEntity;
import com.company.taxi.service.TaxiService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
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
    private final TaxiService taxiService;
    private final ClientService clientService;
    private final AuthService authService;

    @Override
    public void handle(Chat chat, Message message) {

        Long chatId = chat.getId();
        Long userId = message.getFrom().getId();
        Integer messageId = message.getMessageId();

        if (chatId.equals(TAXI_GROUP_ID)) {

//           if (message.isReply()) {
//
//               Message repliedToMessage = message.getReplyToMessage();
//               if (repliedToMessage.getFrom().getId().equals(getBotId())) {
//                   Long taxiChatId = getTaxiChatIdForReply(message);
//
//               }
//
//           }
            return;
        }
        if (taxiService.existsById(chatId)) {
            TaxiEntity taxi = taxiService.getById(chatId);
            if (taxi.getStatus()) {
                return;
            }

        }


        if (message.isCommand()) {
            String text = message.getText();
            if (text.startsWith("/help")) {
                senderService.sendMessage(chatId, Components.HELP, senderService.getGroupUrl());
                return;
            } else {
                senderService.deleteMessage(chatId, message.getMessageId());
            }
        }
        if (!isUserAdmin(chatId, userId)) {
            senderService.replyMessage(chatId, messageId, Components.GROUP_ADS + "\n" + GROUP_LINK, getInlineButtonForGroup());
            senderService.deleteMessage(chatId, message.getMessageId());
        }
    }

    @Override
    public void handleCallBackQuery(Long chatId, CallbackQuery callbackQuery) {

        Message message = callbackQuery.getMessage();
        Chat group = message.getChat();

        String voyageId = callbackQuery
                .getData()
                .split("\n")[1];

        VoyageEntity voyage = clientService
                .getVoyage(Long.valueOf(voyageId));

        TaxiEntity taxi = taxiService.getById(chatId);
        UserEntity userById = authService.getUserById(taxi.getChatId());

        if (taxi.getStatus()) {

            String text = message.getText();
            String [] texts = message.getText()
                    .split("\n");
//            String nameTaxi = "[" + userById.getFullName() + "](tg://user?id=" + chatId + ")";
            String nameTaxi =userById.getFullName();

            if (text.contains(nameTaxi) /*||
                text.contains(userById.getFullName())*/) {
                return;
            }
            String caption = text + "\n" + nameTaxi;
            senderService.sendMenu(userById, voyage.getData());
            InlineKeyboardMarkup markup = senderService.getInlineKeyboardMarkup(Components.ILL_GET, Components.ILL_GET + "\n" + voyageId);

            if (texts.length <= 4) {
                senderService.editMessageMarkdown(group.getId(), message.getMessageId(), caption, markup);
            } else {
                caption+="\n\n" + Components.IS_AGREE;
                senderService.editMessage(group.getId(), message.getMessageId(), caption);
            }

        }
    }

    private Long getTaxiChatIdForReply(Message message) {
        return message.getChat().getId();
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

    @SneakyThrows
    private Long getBotId() {
        GetMe getMe = new GetMe();
        User botUser = senderService.getMe(getMe);
        return botUser.getId();
    }
}
