package com.company.group.services.impl;

import com.company.auth.components.UserEntity;
import com.company.auth.service.AuthService;
import com.company.client.components.VoyageEntity;
import com.company.client.service.ClientService;
import com.company.components.Components;
import com.company.group.components.GroupEntity;
import com.company.group.GroupRepository;
import com.company.group.services.GroupCircular;
import com.company.group.services.GroupService;
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
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
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

    private final GroupCircular groupCircular;

    @Override
    public void handle(Chat chat, Message message) {
        Long chatId = chat.getId();
        Long userId = message.getFrom().getId();
        Integer messageId = message.getMessageId();
        System.out.println(chatId);
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


        groupCircular.isExist(chatId);

        if (taxiService.existsById(userId)) {
            TaxiEntity taxi = taxiService.getById(userId);
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

            String text = message.getText();
            if (message.getText().length() > 30) {
                senderService
                        .sendMessage(chatId, Components.ATTENTION_TAXIST + "\n" + GROUP_LINK, getInlineButtonOrderForGroup(Components.CALL_BOT, BOT_URL));
//                senderService.deleteMessage(chatId, message.getMessageId());
                return;

            } else if(groupCircular.checkKeyWord(text.toLowerCase())) {

                senderService.forwardMessage(userId, messageId, TAXI_GROUP_ID);

                Message executed = senderService
                        .sendMessage(chatId, Components.GROUP_ADS2 + "\n" + GROUP_LINK, getInlineButtonForGroup());
            senderService.deleteMessage(chatId, message.getMessageId());
            return;
            }
            Message executed = senderService
                    .sendMessage(chatId, Components.GROUP_ADS2 + "\n" + GROUP_LINK, getInlineButtonForGroup());
            senderService.deleteMessage(chatId, message.getMessageId());
        }
    }

    @Override
    public void handleCallBackQuery(Long chatId, CallbackQuery callbackQuery) {

        System.out.println(callbackQuery.getData());
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
            String[] texts = message.getText()
                    .split("\n");
//            String nameTaxi = "[" + userById.getFullName() + "](tg://user?id=" + chatId + ")";
            Chat chat = message.getChat();
            String firstName = chat.getFirstName();
            String userName = chat.getUserName();
            userById.setFullName(firstName);
            userById.setUsername(userName);
            authService.save(userById);

            String nameTaxi = userById.getFullName();

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
                caption += "\n\n" + Components.IS_AGREE;
                senderService.editMessage(group.getId(), message.getMessageId(), caption);
            }

        }
    }

    private Long getTaxiChatIdForReply(Message message) {
        return message.getChat().getId();
    }

    public InlineKeyboardMarkup getInlineButtonForGroup() {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton client = InlineKeyboardButton
                .builder()
                .text(Components.GROUP_LINK)
                .url(BOT_URL)
                .build();
        row1.add(client);
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getInlineButtonOrderForGroup(String text, String url) {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        InlineKeyboardButton client = InlineKeyboardButton
                .builder()
                .text(text)
                .url(url)
                .build();
        row1.add(client);
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);

        return inlineKeyboardMarkup;
    }

    @Override
    public List<GroupEntity> getAll() {
        return groupCircular.getAll();
    }

    @Override
    public void giveAdToGroups1() {
        getAll().forEach(group -> {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(group.getGroupId())
                    .protectContent(true)
                    .caption(Components.ATTENTION_ALL_TAXIST_1)
                    .photo(new InputFile(new File(Components.BOT_IMG_PATH)))
                    .replyMarkup(getInlineButtonForGroup())
                    .build();
            Message message = senderService.sendPhoto(sendPhoto);
            Integer messageId = message.getMessageId();
            senderService.pinMessage(group.getGroupId(), messageId);
        });
    }

    @Override
    public void giveAdToGroups2() {
        getAll().forEach(group -> {
            SendPhoto sendPhoto = SendPhoto.builder()
                    .chatId(group.getGroupId())
                    .protectContent(true)
                    .caption(Components.ATTENTION_ALL_TAXIST_2)
                    .photo(new InputFile(new File(Components.BOT_IMG_PATH)))
                    .replyMarkup(getInlineButtonForGroup())
                    .build();
            Message message = senderService.sendPhoto(sendPhoto);
            Integer messageId = message.getMessageId();
            senderService.pinMessage(group.getGroupId(), messageId);
        });
    }

    private boolean isUserAdmin(long chatId, long userId) {

        GetChatAdministrators getChatAdministrators = new GetChatAdministrators();
        getChatAdministrators.setChatId(chatId);
        GetChatMember getChatMember = new GetChatMember();
        getChatMember.setChatId(chatId);
        getChatMember.setUserId(userId);

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
