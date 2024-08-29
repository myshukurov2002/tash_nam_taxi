package com.company.admin;

import com.company.BotController;
import com.company.auth.components.UserEntity;
import com.company.auth.components.UserRole;
import com.company.auth.components.UserState;
import com.company.auth.service.AuthService;
import com.company.components.Components;
import com.company.sender.SenderService;
import com.company.taxi.components.TaxiEntity;
import com.company.taxi.service.TaxiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.commands.DeleteMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.BanChatMember;
import org.telegram.telegrambots.meta.api.methods.groupadministration.CreateChatInviteLink;
import org.telegram.telegrambots.meta.api.methods.groupadministration.UnbanChatMember;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.company.components.Components.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final AuthService authService;
    private final SenderService senderService;
    private final TaxiService taxiService;
    private BotController botController;

    @Value("${admin.username}")
    private String ADMIN_USERNAME;
    @Value("${taxi.group.id}")
    private Long TAXI_GROUP_ID;

    @Transactional
    @Override
    public void handleMessage(UserEntity admin, Message message) {
        String text = message.getText();
        String[] words = text.split(" ");
        Long adminId = admin.getChatId();

        switch (words[0]) {
            case AdminComponents.BAN -> {
                banTaxi(Long.valueOf(words[1]));
                senderService.sendMessage(adminId, SUCCESS);
            }
            case AdminComponents.UNBAN -> {
                unbanTaxi(Long.valueOf(words[1]));
                senderService.sendMessage(adminId, SUCCESS);
            }
            case AdminComponents.ADD_DURATION -> {
                addDuration(Long.valueOf(words[1]), Integer.parseInt(words[2]));
                senderService.sendMessage(adminId, SUCCESS);
            }
            case AdminComponents.GET -> {
                getInfo(Long.valueOf(words[1]), adminId);
            }
        }
    }

    private void getInfo(Long taxiId, Long chatId) {
        TaxiEntity taxi = taxiService.getById(taxiId);
        taxiService.getInfo(taxi, chatId);
    }

    private void addDuration(Long taxiId, int duration) {
        TaxiEntity taxi = taxiService.getById(taxiId);
        taxi.setDuration(duration);
        taxiService.save(taxi);
        senderService.sendMessage(taxiId, AdminComponents.DURATION_EXTENDED + taxi.getDuration() + "kun");
    }

    @Override
    @Transactional
    public void handleCallBackQuery(UserEntity user, CallbackQuery callbackQuery) {

        String data = callbackQuery.getData();
        String[] dataArr = data.split(" ");
        String isApprove = dataArr[0];
        Long taxiId = Long.valueOf(dataArr[1]);

        TaxiEntity taxi = taxiService
                .getById(taxiId);
        UserEntity userById = authService.getUserById(taxiId);

        switch (user.getUserRole()) {
            case ADMIN, SUPER_ADMIN -> {
                switch (isApprove) {
                    case Components.APPROVE -> {
                        taxi.setStatus(true);
                        taxi.setDuration(taxi.getDuration() + 30);
                        taxiService.updateStatus(taxi);
                        senderService.answerInlineButton(callbackQuery.getId(), APPROVE);
//                        senderService.sendMessage(taxiId, Components.APPROVED + taxi.getFromTo());
                        senderService.sendMenu(userById, Components.APPROVED + taxi.getFromTo() + "\n" + Components.AFTER_APPROVE);
                    }
                    case Components.DISAPPROVE -> {

                        userById.setUserRole(UserRole.SOUL);
                        userById.setUserState(UserState.USER_TYPE);
                        authService.save(userById);

                        taxiService.deleteByChatId(taxi);
                        senderService.askUserType(taxiId, Components.DISAPPROVED);

                        senderService.answerInlineButton(callbackQuery.getId(), DISAPPROVE);
                    }
                }
            }
        }
        Message message = callbackQuery.getMessage();

        PhotoSize photo = message.getPhoto()
                .stream()
                .max(Comparator.comparingInt(PhotoSize::getFileSize))
                .orElseThrow();
        String fileId = photo.getFileId();

        String caption = message.getCaption();
        caption += "\n\n" + isApprove;

        SendPhoto sendPhoto = SendPhoto
                .builder()
                .chatId(ADMIN_ID)
                .photo(new InputFile(fileId))
                .caption(caption)
                .build();

//        senderService.sendPhoto(sendPhoto);
        senderService.editMessageWithMedia(message.getMessageId(), sendPhoto, caption);
        senderService.editMessageCaption(user.getChatId(), message.getMessageId(), caption);
//        senderService.deleteMessage(user.getChatId(), callbackQuery.getMessage().getMessageId());
    }

    @Override
    @PostConstruct
    public void init() {
        try {
            UserEntity superAdmin = UserEntity.builder()
                    .chatId(SUPER_ADMIN_ID)
                    .userRole(UserRole.SUPER_ADMIN)
                    .phone("+998902812345")
                    .userState(UserState.REGISTRATION_DONE)
                    .fullName("Muhammad Yusuf")
                    .build();
            superAdmin.setCreatedDate(LocalDateTime.now());

//            authService.save(superAdmin);//TODO

            UserEntity admin = UserEntity.builder()
                    .chatId(ADMIN_ID)
                    .userRole(UserRole.ADMIN)
                    .phone("+998772872345")
                    .userState(UserState.REGISTRATION_DONE)
                    .fullName("Tez Taksi ADMIN")
                    .build();
            admin.setCreatedDate(LocalDateTime.now());

            authService.save(admin);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    @PostConstruct
    public void initCommands() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand("/help", "Yordam"));
        commands.add(new BotCommand("/menu", "Bosh Menu"));

        SetMyCommands setMyCommands = new SetMyCommands();
        setMyCommands.setCommands(commands);
        senderService.initCommands(setMyCommands);

    }

    @PostConstruct
    public void removeCommandsFromAllGroups() {
        DeleteMyCommands deleteMyCommands = new DeleteMyCommands();
        BotCommandScopeAllGroupChats scopeAllGroups = new BotCommandScopeAllGroupChats();
        deleteMyCommands.setScope(scopeAllGroups);
            senderService.execute(deleteMyCommands);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Every day at midnight
    public void autoBanTaxis() {
        List<TaxiEntity> taxis = taxiService.findAll();

        for (TaxiEntity taxi : taxis) {
            if (taxi.getDuration() > 0) {
                taxi.setDuration(taxi.getDuration() - 1);
                taxiService.save(taxi);
            }
            else if (taxi.getDuration() == 0) {
                banTaxi(taxi.getChatId());
                taxi.setStatus(false);
                taxiService.save(taxi);
            }
            if (taxi.getDuration() == 2) {
                senderService.sendMessage(taxi.getChatId(), AdminComponents.CAN_BANNED + ADMIN_ID);
            }
        }
    }

    public void unbanTaxi(Long taxiId) {
        UnbanChatMember unbanChatMember = new UnbanChatMember();
        unbanChatMember.setChatId(TAXI_GROUP_ID); // The ID of the group
        unbanChatMember.setUserId(taxiId);  // The ID of the taxi (user) to unban
        unbanChatMember.setOnlyIfBanned(true);

            senderService.execute(unbanChatMember);
            senderService.sendMessage(taxiId, AdminComponents.UNBANNED);

            CreateChatInviteLink createInviteLink = new CreateChatInviteLink();
            createInviteLink.setChatId(TAXI_GROUP_ID);
//            createInviteLink.setCreatesJoinRequest(true);
            createInviteLink.setName("taxi");
            createInviteLink.setMemberLimit(1);
            Instant expireTime = Instant
                    .now()
                    .plus(1, ChronoUnit.DAYS);
//            createInviteLink.setExpireDate(expireTime.getEpochSecond());
            ChatInviteLink inviteLink = senderService.execute(createInviteLink);

            TaxiEntity taxi = taxiService.getById(taxiId);
            taxi.setStatus(true);
            taxi.setDuration(30);
            taxiService.save(taxi);

            senderService.sendMessage(taxiId, AdminComponents.ENTER_THE_GROUP + inviteLink.getInviteLink());
    }

    private void banTaxi(Long taxiId) {

        TaxiEntity taxi = taxiService.getById(taxiId);
        taxi.ban();
        taxiService.save(taxi);

        BanChatMember banChatMember = new BanChatMember();
        banChatMember.setChatId(TAXI_GROUP_ID);
        banChatMember.setUserId(taxiId);

        senderService.execute(banChatMember);
        senderService.sendMessage(TAXI_GROUP_ID, "Taxi ID: " + taxiId + " banned.");
        senderService.sendMessage(taxiId, AdminComponents.YOUR_BANNED + ADMIN_USERNAME);
    }
}
