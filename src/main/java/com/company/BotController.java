package com.company;

import com.company.client.components.VoyageEntity;
import com.company.components.Components;
import com.company.group.services.GroupService;
import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.GetMe;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllPrivateChats;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class BotController extends TelegramLongPollingBot {

    @Value("${bot.username}")
    private String BOT_USERNAME;
    @Value("${bot.token}")
    private String BOT_TOKEN;

    @Autowired
    private MessageHandler messageHandler;

    @Autowired
    private GroupService groupService;

    public BotController() {
//        setPrivateChatCommands();
//        removeGroupChatCommands();
    }

    @Override
    public void onUpdateReceived(Update update) {

        if (update.hasMessage()) {
            Message message = update.getMessage();
            Chat chat = message.getChat();

            if (chat.isGroupChat() || chat.isSuperGroupChat()) {
                groupService.handle(chat, message);

            } else {
                messageHandler.handleMessage(message);
            }

        } else if (update.hasCallbackQuery()) {

            CallbackQuery callbackQuery = update.getCallbackQuery();
            Long chatId = callbackQuery.getFrom().getId();

            messageHandler.handleCallbackQuery(chatId, callbackQuery);
        }
    }

    public void handle(Message message) {
        messageHandler.handleMessage(message);
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @PostConstruct
    public void registerBot() {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            telegramBotsApi.registerBot(this);
            log.info(".. initialized");
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
    }

    @SneakyThrows
    public void sendCarImg(Long chatId, String imageUrl) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .chatId(chatId)
                .protectContent(true)
                .photo(new InputFile(new File(imageUrl)))
                .build();
        execute(sendPhoto);
    }

    @SneakyThrows
    public Message sendPhoto(SendPhoto sendPhoto) {
            sendPhoto.setParseMode("HTML");
           return execute(sendPhoto);
    }

    public void setPrivateChatCommands() {
        List<BotCommand> commands = new ArrayList<>();
//        commands.add(new BotCommand("/start", Components.START_COMMAND));
        commands.add(new BotCommand("/help", Components.HELP_COMMAND));
        commands.add(new BotCommand("/menu", Components.MAIN_MENU));

        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);
        setMyCommands.setScope(new BotCommandScopeAllPrivateChats());

        try {
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }


    public void removeGroupChatCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/help", Components.HELP_COMMAND));

        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);
        setMyCommands.setScope(new BotCommandScopeAllGroupChats());

        try {
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public void handle(VoyageEntity voyage, Integer messageId) {
        messageHandler.handleVoyage(voyage, messageId);
    }

    @SneakyThrows
    public void ban(BanChatMember banChatMember) {
        execute(banChatMember);
    }

    @SneakyThrows
    public void unban(UnbanChatMember unbanChatMember) {
        execute(unbanChatMember);
    }

    @SneakyThrows
    public ChatInviteLink createInviteLink(CreateChatInviteLink createInviteLink) {
        return execute(createInviteLink);
    }

    @SneakyThrows
    public void removeGroupCommands(DeleteMyCommands deleteMyCommands) {
        execute(deleteMyCommands);
    }

    @SneakyThrows
    public User getMe(GetMe getMe) {
        return execute(getMe);
    }

    public void pinMessage(PinChatMessage pin) {
        try {
            execute(pin);
        } catch (TelegramApiException e) {
                sendMessage(Components.ADMIN_ID, "\nchatId: " + pin.getChatId());
        }
    }

    @SneakyThrows
    private Message sendMessage(long chatId, String msg) {

        SendMessage sendMessage = SendMessage.builder()
                .chatId(chatId)
                .text("<code>" + msg + "</code>")
                .parseMode("html")
                .build();
        return execute(sendMessage);
    }
}
