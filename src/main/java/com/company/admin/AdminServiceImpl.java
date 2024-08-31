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
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeAllGroupChats;

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
    @Value("${card.number}")
    private String CARD_NUMBER;
    @Value("${card.owner}")
    private String CARD_OWNER;
    @Value("${taxi.price}")
    private String TAXI_PRICE;
    @Value("${state.duration}")
    private Integer DURATION;

    @Transactional
    @Override
    public void handleMessage(UserEntity admin, Message message) {
        String text = message.getText();
        String[] words = text.split(" ");
        Long adminId = admin.getChatId();

        try {
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
                    addDuration(Integer.parseInt(words[1]), Long.valueOf(words[2]));
                    senderService.sendMessage(adminId, SUCCESS);
                }
                case AdminComponents.GET -> {
                    getInfo(Long.valueOf(words[1]), adminId);
                }
                case AdminComponents.SEND -> {
                    send(text);

                }
                case AdminComponents.CARD -> {
                    sendCard(Long.valueOf(words[1]));
                }
                default -> {
                    senderService.sendMessage(adminId, WRONG_ANSWER);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            senderService.sendMessage(adminId, "<code>" + e.getMessage() + "</code>");
        }
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
                        taxi.setDuration(taxi.getDuration() + DURATION);
                        taxiService.updateStatus(taxi);
                        senderService.answerInlineButton(callbackQuery.getId(), APPROVE);
//                        senderService.sendMessage(taxiId, Components.APPROVED + taxi.getFromTo());
                        senderService.sendMenu(userById, Components.APPROVED + taxi.getFromTo() + "\n" + Components.AFTER_APPROVE);
                        senderService.sendMenu(userById, AdminComponents.JOIN_GROUP + getInviteLink());
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

    @Scheduled(cron = "0 */1 * * * ?")
    public void autoBanTaxis() {
        List<TaxiEntity> taxis = taxiService
                .findAll();

        for (TaxiEntity taxi : taxis) {
            if (taxi.getDuration() == 2) {
                senderService.sendMessage(taxi.getChatId(), AdminComponents.CAN_BANNED + ADMIN_ID);
                senderService.sendMessage(ADMIN_ID, "sent attenttion to taxi ID: " + taxi.getChatId());
            }

            if (taxi.getDuration() > 0) {
                taxi.setDuration(taxi.getDuration() - 1);
                taxiService.save(taxi);
                senderService.sendMessage(ADMIN_ID, "incremented duration");
            } else if (taxi.getDuration() == 0) {
                banTaxi(taxi.getChatId());
                taxi.setStatus(false);
                taxiService.save(taxi);
                senderService.sendMessage(ADMIN_ID, "banned taxi ID: " + taxi.getChatId());
            }
            senderService.sendMessage(ADMIN_ID, "autoban worked");
        }
    }

    private void send(String text) {
        text.replace("/send", " ");
        authService
                .getAll()
                .forEach((UserEntity u) -> {
                    Long chatId = u.getChatId();
                    senderService.sendMessage(chatId, text);
                });
    }

    private void sendCard(Long chatId) {
        UserEntity userById = authService.getUserById(chatId);
        StringBuilder amount = new StringBuilder()
                .append(AdminComponents.SHOULD_PAY)
                .append("\n\n")
                .append(AdminComponents.CARD_OWNER)
                .append(CARD_OWNER)
                .append("\n")
                .append(AdminComponents.CARD_NUMBER)
                .append(CARD_NUMBER)
                .append("\n")
                .append(AdminComponents.TAXI_PRICE)
                .append(TAXI_PRICE)
                .append("\n\n").
                append("Admin: ")
                .append(ADMIN_USERNAME);
        senderService.sendMenu(userById, amount.toString());
    }

    private void getInfo(Long taxiId, Long chatId) {
        TaxiEntity taxi = taxiService.getById(taxiId);
        taxiService.getInfo(taxi, chatId);
    }

    private void addDuration(int duration, Long taxiId) {
        TaxiEntity taxi = taxiService.getById(taxiId);
        taxi.setDuration(duration);
        taxi.setStatus(true);
        taxiService.save(taxi);

        if (duration > 0) {
            UserEntity userById = authService.getUserById(taxiId);
            senderService.sendMenu(userById, AdminComponents.DURATION_EXTENDED + taxi.getDuration() + "kun");
        }
    }

    public void unbanTaxi(Long taxiId) {
        UnbanChatMember unbanChatMember = new UnbanChatMember();
        unbanChatMember.setChatId(TAXI_GROUP_ID); // The ID of the group
        unbanChatMember.setUserId(taxiId);  // The ID of the taxi (user) to unban
        unbanChatMember.setOnlyIfBanned(true);

        senderService.execute(unbanChatMember);
        senderService.sendMessage(taxiId, AdminComponents.UNBANNED);

        TaxiEntity taxi = taxiService.getById(taxiId);
        taxi.setStatus(true);
        taxi.setDuration(DURATION);
        taxiService.save(taxi);

        senderService.sendMessage(taxiId, AdminComponents.ENTER_THE_GROUP + getInviteLink());
    }

    private String getInviteLink() {

        CreateChatInviteLink createInviteLink = new CreateChatInviteLink();
        createInviteLink.setChatId(TAXI_GROUP_ID);
//            createInviteLink.setCreatesJoinRequest(true);
        createInviteLink.setName("taxi");
        createInviteLink.setMemberLimit(1);
        Instant expireTime = Instant
                .now()
                .plus(1, ChronoUnit.DAYS);

        return senderService
                .execute(createInviteLink)
                .getInviteLink();
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
