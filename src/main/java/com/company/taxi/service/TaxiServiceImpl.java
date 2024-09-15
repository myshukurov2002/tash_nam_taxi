package com.company.taxi.service;

import com.company.attach.service.AttachService;
import com.company.auth.UserRepository;
import com.company.auth.components.UserEntity;
import com.company.auth.components.UserRole;
import com.company.auth.components.UserState;
import com.company.client.components.VoyageEntity;
import com.company.client.service.ClientService;
import com.company.components.Components;
import com.company.expections.exp.AppBadRequestException;
import com.company.sender.SenderService;
import com.company.taxi.TaxiRepository;
import com.company.taxi.components.TaxiEntity;
import com.company.taxi.components.TaxiState;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.company.components.Components.*;
import static com.company.group.GroupServiceImpl.GROUP_IDS;


@Service
@RequiredArgsConstructor
@Slf4j
public class TaxiServiceImpl implements TaxiService {

    private final TaxiRepository taxiRepository;

    private final SenderService senderService;
    private final AttachService attachService;
    private final ClientService clientService;
    private final UserRepository userRepository;

    @Value("${from}")
    private String FROM;
    @Value("${to}")
    private String TO;
    @Value("${taxi.group.id}")
    private String TAXI_GROUP_ID;
    @Value("${admin.username}")
    private String ADMIN_USERNAME;
    @Value("${admin.id}")
    private String ADMIN_ID;
    @Value(("${bot.url}"))
    private String BOT_URL;


    @Override
    public void handleMessage(UserEntity user, Message message) {

        Long chatId = user.getChatId();
        TaxiEntity taxi = getById(chatId);
        TaxiState taxiState = taxi.getTaxiState();
        String text = message.getText();
        Integer messageId = message.getMessageId();

        switch (taxiState) {
            case START -> {
//                senderService.sendMessage(chatId, TAXI_START);
                senderService.sendMessage(chatId, CAR_PHOTO);
                senderService.sendImage(chatId,Components.CAR_IMG_PATH);

                taxi.setTaxiState(TaxiState.CAR_PHOTO);
                taxiRepository.save(taxi);
            }
            case CAR_PHOTO -> {

                if (!message.hasPhoto()) {
                    if (message.hasText() && message.getText().equals(TAXIST)) {
                        senderService.sendMessage(chatId, CAR_PHOTO);
                    } else {
                        senderService.sendMessage(chatId, Components.WRONG_ANSWER);
                        senderService.deleteMessage(chatId, messageId);
                    }
                    return;
                }
                attachService.storing(chatId, message);

                senderService.sendMessage(chatId, Components.SUCCESS);
                taxi.setTaxiState(TaxiState.TAXI_REGISTRATION_DONE);
                taxi.setFromTo(FROM + " - " + TO);
                taxiRepository.save(taxi);

                senderService.deleteMessage(chatId, messageId);
                senderService.sendMenu(user, REQUEST_ADMIN + ADMIN_USERNAME);

                String status = (taxi.getStatus()) ? ALLOWED : NOT_ALLOWED;
                sendRequestToAdmin(
                        getInfo(user, taxi, status)
                );
            }
            case TAXI_REGISTRATION_DONE -> {

                switch (text) {

                    case PROFILE_INFO -> {

                        getInfo(taxi, taxi.getChatId());
                    }
                    case MAIN_MENU, "/menu" -> {
                        user.setUserState(UserState.USER_TYPE);
                        user.setUserRole(UserRole.SOUL);
                        userRepository.save(user);

                        senderService.askUserType(chatId, MAIN_MENU);
                    }

                    case CONNECT_ADMIN -> senderService.sendMessage(chatId, HELP, getMenu());
                    case GIVE_ADD -> {
                        senderService.sendMessage(chatId, Components.TAXI_ADS + "\n\n\n" + Components.TAXI_ADS_EXAMPLE);
                        taxi.setTaxiState(TaxiState.GIVE_ADD);
                        save(taxi);
                    }
                    default -> {
                        if (text.equals(TAXIST)) {
                            if (taxi.getTaxiState().equals(TaxiState.TAXI_REGISTRATION_DONE)) {
                                user.setUserRole(UserRole.TAXIST);
                                userRepository.save(user);
                                senderService.sendMenu(user, TAXI_MENU);
                                return;
                            }
                        } else if (text.equals(CLIENT)) {
                            if (taxi.getTaxiState().equals(TaxiState.TAXI_REGISTRATION_DONE)) {
                                user.setUserRole(UserRole.CLIENT);
                                userRepository.save(user);
                                senderService.sendMenu(user, CLIENT_MENU);
                                return;
                            }
                        }
//                        taxi.setVisibility(false);
//                        taxiRepository.save(taxi);
                        senderService.deleteMessage(chatId, messageId);
//                senderService.sendMessage(chatId, Components.SHOULD_FILL);
//                handleMessage(user, message);
                    }
                }
            }
            case GIVE_ADD -> {
                sendAds(taxi, message);
                taxi.setTaxiState(TaxiState.TAXI_REGISTRATION_DONE);
                save(taxi);
                senderService.sendMessage(taxi.getChatId(), SUCCESS, getMenu());
            }

            default -> {
                if (text.equals(TAXIST)) {
                    if (taxi.getTaxiState().equals(TaxiState.TAXI_REGISTRATION_DONE)) {
                        user.setUserRole(UserRole.TAXIST);
                        userRepository.save(user);
                        senderService.sendMenu(user, TAXI_MENU);
                        return;
                    }
                } else if (text.equals(CLIENT)) {
                    if (taxi.getTaxiState().equals(TaxiState.TAXI_REGISTRATION_DONE)) {
                        user.setUserRole(UserRole.CLIENT);
                        userRepository.save(user);
                        senderService.sendMenu(user, CLIENT_MENU);
                        return;
                    }
                }
//                taxi.setVisibility(false);
//                taxiRepository.save(taxi);
                senderService.deleteMessage(chatId, messageId);
//                handleMessage(user, message);
//                senderService.sendMessage(chatId, Components.SHOULD_FILL);
            }
        }
    }

    private void sendAds(TaxiEntity taxi, Message message) {

        GROUP_IDS.forEach(groupId -> {

            SendPhoto sendPhoto = attachService.getSendPhoto(taxi.getChatId());

            sendPhoto.setChatId(groupId);
            sendPhoto.setCaption(String.valueOf(message.getText()));
            sendPhoto.setProtectContent(true);
            sendPhoto.setReplyMarkup(getAds());
            senderService.sendPhoto(sendPhoto);
        });
    }

    private ReplyKeyboard getAds() {

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton client = senderService.getInlineButton(Components.BOT_ADS, BOT_URL);
        InlineKeyboardButton taxi = senderService.getInlineButton(BE_TAXI, BOT_URL);
        row.add(client);
        rows.add(row);

        //TODO
//        row = new ArrayList<>();
//        row.add(taxi);
//        rows.add(row);
//        markup.setKeyboard(rows);

        return markup;
    }

    public void getInfo(TaxiEntity taxi, Long chatId) {

        UserEntity user = userRepository
                .findByChatIdAndVisibilityTrue(taxi.getChatId())
                .orElseThrow();

        String status = (taxi.getStatus()) ? ALLOWED : NOT_ALLOWED;
        SendPhoto sendPhoto = getInfo(user, taxi, status);
        sendPhoto.setChatId(chatId);
        senderService.sendPhoto(sendPhoto);

        if (status.equals(NOT_ALLOWED))
            senderService.sendMessage(chatId, Components.NOT_ALLOWED_2, getMenu());
    }

    @Override
    public boolean existsById(Long chatId) {

        return taxiRepository
                .existsById(chatId);
    }


    @Override
    public void handleCallbackQuery(UserEntity user, CallbackQuery callbackQuery) {

        TaxiEntity taxi = getById(user.getChatId());
        Long chatId = user.getChatId();

        TaxiState taxiState = taxi.getTaxiState();
        String data = callbackQuery.getData();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        senderService.sendMenu(user, TAXI_MENU);
    }

    public void sendRequestToAdmin(SendPhoto taxiInfo) {

        final String taxiId = taxiInfo.getChatId();
        taxiInfo.setChatId(ADMIN_ID);
        taxiInfo.setReplyMarkup(getApproveInlineButtons(taxiId));
        senderService.sendPhoto(taxiInfo);
    }

    @Override
    public TaxiEntity getById(Long chatId) {

        return taxiRepository
                .findByChatIdAndVisibilityTrue(chatId)
                .orElseGet(() -> {
                    TaxiEntity taxi = TaxiEntity
                            .builder()
                            .chatId(chatId)
                            .taxiState(TaxiState.START)
                            .build();

                    return taxiRepository.save(taxi);
                });
    }

    @SneakyThrows
    @Override
    public void updateStatus(TaxiEntity taxi) {
        taxiRepository.save(taxi);
    }

    @SneakyThrows
    @Transactional
    @Override
    public void deleteByChatId(TaxiEntity taxi) {
        attachService.deleteByChatId(taxi.getChatId());
        taxiRepository.deleteById(taxi.getChatId());
    }

    @Override
    public void sendVoyage(VoyageEntity voyage, Integer messageId) {//TODO
        System.out.println(TAXI_GROUP_ID);

        String[] data = voyage
                .getData()
                .split("\n");
        String temp = data[0] + "\n" + data[1] + "\n\n" + Components.WILL_ORDER;
        senderService.sendToTaxiGroup(Long.valueOf(TAXI_GROUP_ID), temp, voyage);
//        List<TaxiEntity> taxistsByVoyage = getTaxistsByVoyage(voyage);
//        for (TaxiEntity taxi : taxistsByVoyage) {
//            senderService.sendMainMenuAndGetExecuted(taxi.getChatId(), voyage.getAbout());
//            senderService.pinMessage(taxi.getChatId(), messageId + 2);
//        }
    }

    @Override
    public List<TaxiEntity> findAll() {
        return taxiRepository
                .findAllByStatusTrue();
    }

    @Override
    public void save(TaxiEntity taxi) {
        try {
            taxiRepository.save(taxi);
        } catch (DataAccessException e) {
            log.error(e.getMessage());
            throw new AppBadRequestException();
        }
    }

    public ReplyKeyboardMarkup getMenu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

//        row.add(Components.GIVE_ADD);
        row.add(PROFILE_INFO);
        row.add(Components.MAIN_MENU);

        rows.add(row);
        row = new KeyboardRow();

        row.add(Components.CONNECT_ADMIN);
        rows.add(row);

        replyKeyboardMarkup.setResizeKeyboard(true);
//        replyKeyboardMarkup.setOneTimeKeyboard(true);
//        replyKeyboardMarkup.setSelective(true);
//        replyKeyboardMarkup.setIsPersistent(true);
        replyKeyboardMarkup.setKeyboard(rows);

        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup getApproveInlineButtons(String taxiId) {


        InlineKeyboardButton approve = InlineKeyboardButton
                .builder()
                .text(Components.APPROVE)
                .callbackData(Components.APPROVE + " " + taxiId)
                .build();

        InlineKeyboardButton disapprove = InlineKeyboardButton
                .builder()
                .text(Components.DISAPPROVE)
                .callbackData(Components.DISAPPROVE + " " + taxiId)
                .build();

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(approve);
        row.add(disapprove);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        return InlineKeyboardMarkup
                .builder()
                .keyboard(rows)
                .build();
    }

    private SendPhoto getInfo(UserEntity user, TaxiEntity taxi, String status) {

        Long chatId = user.getChatId();

        SendPhoto sendPhoto = attachService.getSendPhoto(chatId);

        String fullName = user.getFullName();
        String phone = Components.PHONE + user.getPhone();
        String addresses = Components.ROADLESS + taxi.getFromTo();

        StringBuilder info = new StringBuilder()
                .append(Components.CHAT_ID).append(chatId).append("\n");

        if (fullName != null) {
            info.append(FULL_NAME)
                    .append("<a href=\"")
                    .append(Components.TELEGRAM_LINK)
                    .append(user.getUsername())
                    .append("\">")
                    .append(fullName)
                    .append("</a>");
        } else {
            info.append(FULL_NAME).append(fullName);
        }

        info.append("\n")
                .append(phone)
                .append("\n")
                .append(addresses)
                .append("\n")
                .append(status)
                .append("\n")
                .append("\n")
                .append(Components.TIME + taxi.getDuration() + "kun");

        sendPhoto.setCaption(String.valueOf(info));
        sendPhoto.setProtectContent(true);
        sendPhoto.setReplyMarkup(getMenu());

        return sendPhoto;
    }
}
