package com.company.admin;

import com.company.BotController;
import com.company.auth.components.UserEntity;
import com.company.auth.components.UserRole;
import com.company.auth.components.UserState;
import com.company.auth.service.AuthService;
import com.company.client.service.ClientService;
import com.company.components.Components;
import com.company.group.services.GroupService;
import com.company.group.services.impl.GroupServiceImpl;
import com.company.sender.SenderService;
import com.company.taxi.components.TaxiEntity;
import com.company.taxi.service.TaxiService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.company.components.Components.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    @Value("${admin.name}")
    private String ADMIN_NAME;
    //    @Value("${admin.phone}")
//    private String ADMIN_PHONE;
    @Value("${admin.id}")
    private Long ADMIN_ID;

    private final AuthService authService;
    private final SenderService senderService;
    private final TaxiService taxiService;
    private final ClientService clientService;
    private final GroupService groupService;
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
        System.out.println(adminId);
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
                case AdminComponents.GET_ALL -> {
                    getAll(adminId);
                }
                case AdminComponents.GET_ALL2 -> {
                    getAll2(adminId);
                }
                case AdminComponents.SEND -> {
                    send(text);

                }
                case AdminComponents.CARD -> {
                    sendCard(Long.valueOf(words[1]));
                }
                case AdminComponents.DELETE -> {
                    delete(Long.valueOf(words[1]));
                }
                case AdminComponents.LINK -> {
                    sendLink(Long.valueOf(words[1]));
                }
                case AdminComponents.PROMOTE_ADMIN -> {
                    promoteRole(Long.valueOf(words[1]), words[2]);
                }
                case AdminComponents.RUN -> {
                    run(text.substring(5));
                }
                case AdminComponents.ADS -> {
                        adToGroup(words[1]);
                }
                default -> {
                    senderService.sendMessage(adminId, AdminComponents.COMMANDS);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getMessage());
            senderService.sendMessage(adminId, "<code>" + e.getMessage() + "</code>");
        }
    }

    private void adToGroup(String key) {
        if (key.equals("1")) {
            groupService.giveAdToGroups1();
        } else {
            groupService.giveAdToGroups2();
        }
    }

    private void getAll2(Long adminId) {
        StringBuilder builder = new StringBuilder();

        authService.getAll()
                .forEach(u -> {

                    builder
                            .append("\nID: " + u.getChatId())
                            .append("\nFullname: " + u.getFullName())
                            .append("\nPhone: " + u.getPhone())
                            .append("\nUsername: " + u.getUsername())
                            .append("\nRole: " + u.getUserRole())
                            .append("\n-------------------------");
                });
        senderService.sendMessage(adminId, builder.toString());
    }

    private void run(String userCommand) {
        try {
            Process proc = Runtime.getRuntime().exec(userCommand);

            BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

            StringBuilder outputBuilder = new StringBuilder();
            String line;

            // Send the first line of the output
            if ((line = reader.readLine()) != null) {
                Message message = senderService.sendMessage(SUPER_ADMIN_ID, "<code>" + line + "</code>");

                while ((line = reader.readLine()) != null) {
                    outputBuilder.append(line).append("\n");

                    if (outputBuilder.length() >= 3000) {
                        senderService.editMessage(SUPER_ADMIN_ID, message.getMessageId(), "<code>" + outputBuilder.toString() + "</code>");
                        outputBuilder.setLength(0); // Clear the buffer
                    }
                }

                if (!outputBuilder.isEmpty()) {
                    senderService.editMessage(SUPER_ADMIN_ID, message.getMessageId(), "<code>" + outputBuilder.toString() + "</code>");
                }
            }

            // Read and handl error stream (if there is an error)
            while ((line = errorReader.readLine()) != null) {
                senderService.sendMessage(SUPER_ADMIN_ID, "<code>Error: " + line + "</code>");
            }

            proc.waitFor();
        } catch (IOException | InterruptedException e) {
            senderService.sendMessage(SUPER_ADMIN_ID, "<code>Command failed: " + e.getMessage() + "</code>");
            throw new RuntimeException(e);
        }
    }

    @Transactional
    protected void promoteRole(Long userId, String role) {

        if (Objects.equals(userId, SUPER_ADMIN_ID)) return;

        delete(userId);
        authService.create(userId, role);

    }

    private void sendLink(Long chatId) {
        TaxiEntity byId = taxiService.getById(chatId);
        if (byId.getStatus()) {
            unban(TAXI_GROUP_ID, chatId);
            senderService.sendMessage(chatId, getInviteLink());
        }
    }

    @Transactional
    void delete(Long chatId) {
        TaxiEntity taxi = taxiService.getById(chatId);

        try {
            unban(TAXI_GROUP_ID, chatId);
            taxiService.deleteByChatId(taxi);
            clientService.deleteByChatId(chatId);
            authService.deleteByChatId(chatId);
            senderService.sendMessage(ADMIN_ID, "deleted ID: " + chatId);
        } catch (Exception e) {
            log.error(e.getMessage());
            senderService.sendMessage(ADMIN_ID, "<code>" + e.getMessage() + "</code>");
        }


    }

    private void getAll(Long adminId) {
        StringBuilder builder = new StringBuilder();

        taxiService
                .findAll()
                .forEach(t -> {
                    UserEntity userById = authService.getUserById(t.getChatId());
                    builder
                            .append("\nID: " + userById.getChatId())
                            .append("\nFullname: " + userById.getFullName())
                            .append("\nPhone: " + userById.getPhone())
                            .append("\nUsername: " + userById.getUsername())
                            .append("\nMuddat: " + t.getDuration())
                            .append("\n-------------------------");
                });
        senderService.sendMessage(adminId, builder.toString());
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
                    .userRole(UserRole.ADMIN)
                    .phone("+998902812345")
                    .userState(UserState.REGISTRATION_DONE)
                    .fullName("Muhammad Yusuf")
                    .build();
            superAdmin.setCreatedDate(LocalDateTime.now());

            authService.save(superAdmin);//TODO

            UserEntity admin = UserEntity.builder()
                    .chatId(ADMIN_ID)
                    .userRole(UserRole.ADMIN)
//                    .phone(ADMIN_PHONE)
                    .userState(UserState.REGISTRATION_DONE)
                    .fullName(ADMIN_NAME)
                    .build();
            admin.setCreatedDate(LocalDateTime.now());

            authService.save(admin);
            System.out.println("running ..");
            senderService.sendMessage(SUPER_ADMIN_ID, "running ..");
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
            senderService.sendMessage(taxiId, AdminComponents.DURATION_EXTENDED + taxi.getDuration() + "kun");
            senderService.sendMenu(userById, AdminComponents.JOIN_GROUP + getInviteLink());
        }
    }

    @Transactional
    public void unbanTaxi(Long taxiId) {
        unban(TAXI_GROUP_ID, taxiId);
        senderService.sendMessage(taxiId, AdminComponents.UNBANNED);

        TaxiEntity taxi = taxiService.getById(taxiId);
        taxi.setStatus(true);
        taxi.setDuration(DURATION);
        taxiService.save(taxi);

        senderService.sendMessage(taxiId, AdminComponents.ENTER_THE_GROUP + getInviteLink());
    }

    private void unban(Long taxiGroupId, Long taxiId) {
        UnbanChatMember unbanChatMember = new UnbanChatMember();
        unbanChatMember.setChatId(taxiGroupId); // The ID of the group
        unbanChatMember.setUserId(taxiId);  // The ID of the taxi (user) to unban
        unbanChatMember.setOnlyIfBanned(true);

        senderService.execute(unbanChatMember);
    }

    private String getInviteLink() {

        CreateChatInviteLink createInviteLink = new CreateChatInviteLink();
        createInviteLink.setChatId(TAXI_GROUP_ID);
        createInviteLink.setName("taxi");
        createInviteLink.setMemberLimit(1);
        Instant expireTime = Instant
                .now()
                .plus(1, ChronoUnit.DAYS);
        return senderService
                .execute(createInviteLink)
                .getInviteLink();
    }

    @Override
    public void banTaxi(Long taxiId) {

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
