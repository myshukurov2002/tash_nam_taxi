package com.company.sender;

import com.company.auth.components.UserEntity;
import com.company.client.components.VoyageEntity;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public interface SenderService {
    void askContactRequest(Long chatId, String askContact);

    void sendMessage(SendMessage sendMessage);

    void sendMessage(Long chatId, String message, ReplyKeyboardMarkup menu);

    void sendMessage(Long chatId, String text);

    void askUserType(Long chatId, String askUserType);

    void initCommands(SendPhoto sendPhoto);

    File initCommands(GetFile getFile);

    void sendKeyboardButton(Long chatId, String... menuButtons);

    void sendCarImg(Long chatId);

    void deleteMessage(Long chatId, Integer messageId);

    void sendMessage(Long chatId, String text, InlineKeyboardMarkup inlineButton);

    void answerInlineButton(String InlineButtonId, String text);

    void editMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup replyMarkup);

    List<ChatMember> getAdmins(GetChatAdministrators chatId);

    void editMessage(Long chatId, Integer messageId, String text);

    void sendPhoto(SendPhoto sendPhoto);

    void sendMenu(UserEntity user, String mainMenu);

    void handle(Message message);

    void sendMessageWithMenu(Long chatId, String text);

    void forwardMessage(Long from, Integer messageId, Long to);

    void editMessageWithMedia(Integer messageId, SendPhoto sendPhoto, String caption);

    void editMessageCaption(Long chatId, Integer messageId, String caption);
    InlineKeyboardMarkup getAddresses();
    InlineKeyboardButton getInlineButton(String text, String data);

    InlineKeyboardMarkup addCancelButton(InlineKeyboardMarkup addresses);

    InlineKeyboardMarkup getGroupUrl();

    void pinMessage(Long chatId, Integer messageId);

    void handle(VoyageEntity voyage, Integer messageId);

    void sendMainMenu(Long chatId, String about);

    Message sendMainMenuAndGetExecuted(Long chatId, String about);

    void initCommands(SetMyCommands setMyCommands);

    void sendToTaxiGroup(Long chatId, String data);

    void execute(BanChatMember banChatMember);

    void execute(UnbanChatMember unbanChatMember);

    ChatInviteLink execute(CreateChatInviteLink createInviteLink);

    void execute(DeleteMyCommands deleteMyCommands);
}
