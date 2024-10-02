package com.company.sender;

import com.company.BotController;
import com.company.auth.components.UserEntity;
import com.company.auth.components.UserRole;
import com.company.client.components.VoyageEntity;
import com.company.components.Components;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.ForwardMessage;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.ChatInviteLink;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.company.components.Components.ILL_GET;
import static com.company.components.Components.PROFILE_INFO;

@Component
@RequiredArgsConstructor
@Slf4j
public class SenderServiceImpl implements SenderService {

    @Value("${bot.url}")
    private String GROUP_LINK;
    @Lazy
    @Autowired
    private BotController botController;

    @Value("${one.two}")
    private String ONE_TWO;
    @Value("${two.one}")
    private String TWO_ONE;

    @Override
    public void handle(Message message) {
        botController.handle(message);
    }

    @Override
    public void handle(VoyageEntity voyage, Integer messageId) {
        botController.handle(voyage, messageId);
    }

    public void askContactRequest(Long chatId, String askContact) {

        SendMessage sendMessage = getSendMessage(chatId, askContact);
        sendMessage.setReplyMarkup(
                getReplyKeyboardMarkup(
                        getRequestContact(askContact)));

        sendMessage(sendMessage);
    }

    @SneakyThrows
    public Message sendMessage(SendMessage sendMessage) {
        return botController.execute(sendMessage);
    }

    @SneakyThrows
    public Message sendMessage(Long chatId, String message) {
        return botController.execute(getSendMessage(chatId, message));
    }

    @SneakyThrows
    public void sendMessage(Long chatId, String message, ReplyKeyboardMarkup replyKeyboardMarkup) {
        SendMessage sendMessage = getSendMessage(chatId, message);
        sendMessage.setReplyMarkup(replyKeyboardMarkup);
        botController.execute(sendMessage);
    }

    @Override
    @SneakyThrows
    public void initCommands(SendPhoto sendPhoto) {
        botController.execute(sendPhoto);
    }

    @Override
    @SneakyThrows
    public File initCommands(GetFile getFile) {
        return botController.execute(getFile);
    }

    @Override
    public void sendKeyboardButton(Long chatId, String... texts) {

        KeyboardRow keyboardRow = getKeyboardRow(texts);
        ReplyKeyboardMarkup replyKeyboardMarkup =
                getReplyKeyboardMarkup(keyboardRow);

        sendMessage(chatId, Components.OVERVIEW, replyKeyboardMarkup);
    }

    @Override
    public void sendImage(Long chatId, String imagePath) {
        botController.sendCarImg(chatId, imagePath);
    }

    @Override
    public void deleteMessage(Long chatId, Integer messageId) {
        DeleteMessage deleteMessage = DeleteMessage.builder()
                .chatId(chatId)
                .messageId(messageId).build();

        try {
            botController.execute(deleteMessage);
        } catch (TelegramApiException e) {
            log.error(e.getMessage() + "The message already deleted!");
        }
    }

    @Override
    public Message sendMessage(Long chatId, String text, InlineKeyboardMarkup inlineButton) {
        SendMessage sendMessage = getSendMessage(chatId, text);
        sendMessage.setParseMode("HTML");
        sendMessage.setReplyMarkup(inlineButton);
        return sendMessage(sendMessage);
    }

    @SneakyThrows
    @Override
    public synchronized void answerInlineButton(String callbackQueryId, String text) {
        AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
                .text(text)
                .callbackQueryId(callbackQueryId)
                .showAlert(true)
                .build();
        botController.execute(answer);
    }

    @SneakyThrows
    @Override
    public void editMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup replyMarkup) {

        EditMessageText edit = getEditMessage(chatId, messageId, text);
        edit.setReplyMarkup(replyMarkup);

        botController.execute(edit);
    }

    @SneakyThrows
    @Override
    public List<ChatMember> getAdmins(GetChatAdministrators getChatAdministrators) {
        return botController.execute(getChatAdministrators);
    }

    @SneakyThrows
    @Override
    public void editMessage(Long chatId, Integer messageId, String text) {
        EditMessageText editMessage = getEditMessage(chatId, messageId, text);
        botController.execute(editMessage);
    }

    @Override
    public Message sendPhoto(SendPhoto sendPhoto) {
        sendPhoto.setParseMode("html");
        return botController.sendPhoto(sendPhoto);
    }

    @Override
    public void sendMenu(UserEntity user, String mainMenu) {
        Long chatId = user.getChatId();
        if (Objects.requireNonNull(user.getUserRole()) == UserRole.TAXIST)
             sendMessage(chatId, mainMenu, getTaxiMenu());
        else sendMessage(chatId, mainMenu, getClientMenu());
    }

    private ReplyKeyboardMarkup getClientMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(Components.CALL_TAXI);
        rows.add(row);
        row = new KeyboardRow();

        row.add(Components.VOYAGES);
        row.add(Components.MAIN_MENU);
//        row.add(Components.CONNECT_ADMIN);
        rows.add(row);

        replyKeyboardMarkup.setResizeKeyboard(true);
//        replyKeyboardMarkup.setOneTimeKeyboard(true);
//        replyKeyboardMarkup.setSelective(true);
//        replyKeyboardMarkup.setIsPersistent(true);
        replyKeyboardMarkup.setKeyboard(rows);

        return replyKeyboardMarkup;
    }

    @Override
    public void sendMessageWithMenu(Long chatId, String text) {
        SendMessage sendMessage = getSendMessage(chatId, text);
        sendMessage.setReplyMarkup(getTaxiMenu());
        sendMessage(sendMessage);
    }

    @SneakyThrows
    @Override
    public void forwardMessage(Long from, Integer messageId, Long to) {
        ForwardMessage forwardMessage = ForwardMessage.builder()
                .chatId(to)
//                .disableNotification(true)
                .fromChatId(from)
                .messageId(messageId)
                .build();
        botController.execute(forwardMessage);
    }

    @Override
    public void editMessageWithMedia(Integer messageId, SendPhoto sendPhoto, String caption) {
        //soon
    }

    @SneakyThrows
    @Override
    public void editMessageCaption(Long chatId, Integer messageId, String caption) {
        EditMessageCaption editMessageCaption = EditMessageCaption
                .builder()
                .chatId(chatId)
                .messageId(messageId)
                .caption(caption)
                .build();
        botController.execute(editMessageCaption);
    }

    @Override
    public InlineKeyboardMarkup getAddresses() {

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton oneTwo = getInlineButton(ONE_TWO, ONE_TWO);
        row.add(oneTwo);
        rows.add(row);
        row = new ArrayList<>();
        InlineKeyboardButton twoOne = getInlineButton(TWO_ONE, TWO_ONE);
        row.add(twoOne);
        rows.add(row);

//        row = new ArrayList<>();
//        InlineKeyboardButton zarTosh = getInlineButton("Zarafshon", "Toshkent");
//        row.add(zarTosh);
//        rows.add(row);
//
//        row = new ArrayList<>();
//        InlineKeyboardButton toshZar = getInlineButton("Toshkent", "Zarafshon");
//        row.add(toshZar);
//        rows.add(row);
//
//        row = new ArrayList<>();
//        InlineKeyboardButton zarNav = getInlineButton("Zarafshon", "Navoiy");
//        row.add(twoOne);
//        rows.add(row);
//
//        row = new ArrayList<>();
//        InlineKeyboardButton navZar = getInlineButton("Navoiy", "Zarafshon");
//        row.add(twoOne);
//        rows.add(row);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    @Override
    public InlineKeyboardButton getInlineButton(String text, String data) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(data)
                .build();
    }

    @Override
    public InlineKeyboardMarkup addCancelButton(InlineKeyboardMarkup addresses) {
        List<List<InlineKeyboardButton>> keyboard = addresses.getKeyboard();
        InlineKeyboardButton cancelButton = getInlineButton(Components.CANCEL_RIDING, Components.CANCEL_RIDING);
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(cancelButton);
        keyboard.add(row);
        addresses.setKeyboard(keyboard);

        return addresses;
    }

    @Override
    public InlineKeyboardMarkup getGroupUrl() {
        InlineKeyboardButton addGroupButton = InlineKeyboardButton
                .builder()
                .text(Components.ADD_GROUP)
                .url(GROUP_LINK + Components.GROUP_URL_START)
                .build();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(addGroupButton);
        rows.add(row);
        return new InlineKeyboardMarkup(rows);
    }

    @SneakyThrows
    @Override
    public void pinMessage(Long chatId, Integer messageId) {
        PinChatMessage pin = PinChatMessage.builder()
                .chatId(chatId)
                .messageId(messageId).build();
        botController.pinMessage(pin);
    }

    @Override
    public void sendMainMenu(Long chatId, String about) {
        askUserType(chatId, about);
    }

    @Override
    public Message sendMainMenuAndGetExecuted(Long chatId, String about) {
        return null;
    }

    @Override
    @SneakyThrows
    public void initCommands(SetMyCommands setMyCommands) {
        botController.execute(setMyCommands);
    }

    @Override
    public Message sendToTaxiGroup(Long chatId, String data, VoyageEntity voyage) {
        SendMessage sendMessage = getSendMessage(chatId, data);
        InlineKeyboardMarkup markup = getInlineKeyboardMarkup(ILL_GET, ILL_GET + "\n" + voyage.getId());
        sendMessage.setParseMode("html");
        sendMessage.enableHtml(true);
        sendMessage.setProtectContent(true);
        sendMessage.setReplyMarkup(markup);
        return sendMessage(sendMessage);
    }

    @Override
    @SneakyThrows
    public void execute(BanChatMember banChatMember) {
        botController.ban(banChatMember);
    }

    @Override
    @SneakyThrows
    public void execute(UnbanChatMember unbanChatMember) {
        botController.unban(unbanChatMember);
    }

    @Override
    public ChatInviteLink execute(CreateChatInviteLink createInviteLink) {
        return botController.createInviteLink(createInviteLink);
    }

    @Override
    public void execute(DeleteMyCommands deleteMyCommands) {
        botController.removeGroupCommands(deleteMyCommands);
    }

    @Override
    public User getMe(GetMe getMe) {
        return botController.getMe(getMe);
    }

    @Override
    @SneakyThrows
    public void editMessageMarkdown(Long id, Integer messageId, String caption, InlineKeyboardMarkup markup) {
        EditMessageText editMessage = getEditMessage(id, messageId, caption);
        editMessage.setParseMode("MarkdownV2");
        editMessage.setReplyMarkup(markup);
        botController.execute(editMessage);
    }

    @Override
    public void replyMessage(Long chatId, Integer messageId, String text, InlineKeyboardMarkup inlineButtonForGroup) {
        SendMessage sendMessage = getSendMessage(chatId, text);
        sendMessage.setReplyMarkup(inlineButtonForGroup);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage(sendMessage);
    }

    @Override
    public InlineKeyboardButton getInlineUrlButton(String text, String url) {
        return InlineKeyboardButton.builder()
                .text(text)
                .url(url)
                .build();
    }

    @Override
    public void sendLongMessage(Long chatId, String text) {
        final int CHUNK_SIZE = 4096;
        int textLength = text.length();
        System.out.println("textLength = " + textLength);
        for (int i = 0; i < textLength; i += CHUNK_SIZE) {
            String chunk = text.substring(i, Math.min(textLength, i + CHUNK_SIZE));
            sendMessage(chatId, "<code>" + chunk + "</code>");
        }
    }

    public void askUserType(Long chatId, String askUserType) {

        SendMessage sendMessage = getSendMessage(chatId, askUserType);
        sendMessage.setReplyMarkup(
                getReplyKeyboardMarkup(
                        getKeyboardRow(Components.TAXIST, Components.CLIENT)
                ));

        sendMessage(sendMessage);
    }

    private SendMessage getSendMessage(Long chatId, String message) {
        return SendMessage.builder()
                .chatId(chatId)
                .replyMarkup(new ReplyKeyboardRemove(true))
                .parseMode("HTML")
                .text(message).build();
    }

    private KeyboardRow getRequestContact(String text) {

        KeyboardButton requestContact = getKeyboardButton(text);
        requestContact.setRequestContact(true);
        KeyboardRow row = new KeyboardRow();
        row.add(requestContact);
        return row;
    }

    private KeyboardButton getKeyboardButton(String text) {
        return KeyboardButton.builder()
                .text(text)
                .build();
    }

    @Override
    public InlineKeyboardMarkup getInlineKeyboardMarkup(String text, String data) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        InlineKeyboardButton inlineButton = getInlineButton(text, data);
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(inlineButton);
        rows.add(row);
        markup.setKeyboard(rows);
        return markup;
    }

    private KeyboardRow getKeyboardRow(String... texts) {

        KeyboardRow keyboardRow = new KeyboardRow();
        for (String text : texts) {
            keyboardRow.add(text);
        }
        return keyboardRow;
    }

    public ReplyKeyboardMarkup getTaxiMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

//        row.add(Components.GIVE_ADD);
        row.add(PROFILE_INFO);
        row.add(Components.MAIN_MENU);
        rows.add(row);
        row = new KeyboardRow();
//  TODO
        row.add(Components.CONNECT_ADMIN);
        rows.add(row);

        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setIsPersistent(true);
        replyKeyboardMarkup.setKeyboard(rows);

        return replyKeyboardMarkup;
    }

    private ReplyKeyboardMarkup getReplyKeyboardMarkup(KeyboardRow... keyboardRows) {
        return ReplyKeyboardMarkup.builder()
                .keyboard(List.of(keyboardRows))
                .resizeKeyboard(true)
                .build();
    }

    private EditMessageText getEditMessage(Long chatId, Integer messageId, String text) {
        return EditMessageText.builder()
                .chatId(chatId)
                .text(text)
                .parseMode("HTML")
                .messageId(messageId)
                .build();
    }
}
