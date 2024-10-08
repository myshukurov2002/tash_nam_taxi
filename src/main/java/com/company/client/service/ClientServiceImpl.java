package com.company.client.service;

import com.company.auth.UserRepository;
import com.company.auth.components.UserEntity;
import com.company.auth.components.UserRole;
import com.company.auth.components.UserState;
import com.company.client.ClientRepository;
import com.company.client.VoyageRepository;
import com.company.client.components.ClientEntity;
import com.company.client.components.ClientState;
import com.company.client.components.VoyageEntity;
import com.company.client.components.VoyageState;
import com.company.components.Components;
import com.company.expections.exp.ItemNotFoundException;
import com.company.sender.SenderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import static com.company.components.Components.*;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;
    private final VoyageRepository voyageRepository;
    private final SenderService senderService;

    @Value("${one.two}")
    private String ONE_TWO;
    @Value("${two.one}")
    private String TWO_ONE;


    @Override
    public void handleMessage(UserEntity user, Message message) {

        Long chatId = user.getChatId();
        String text = message.getText();
        ClientEntity client = getById(chatId);
        ClientState state = client.getState();
        Integer messageId = message.getMessageId();

        System.out.println(message.getText());
        senderService.sendMessage(chatId, message.getText());

        switch (state) {
            case START -> {

                client.setState(ClientState.CLIENT_MENU);
                clientRepository.save(client);

                senderService.sendMenu(user, CLIENT_MENU);

            }
            case CLIENT_MENU -> {
                switch (text) {
                    case TAXIST -> {
                        user.setUserRole(UserRole.TAXIST);
                        userRepository.save(user);
                        senderService.handle(message);
                        return;
                    }
                    case CLIENT -> {
                        senderService.sendMenu(user, CLIENT_MENU);
                        return;
                    }
                    case CALL_TAXI -> {
                        InlineKeyboardMarkup keyboard = getAddress();

                        senderService.sendMessage(chatId, CALL_TAXI, getBackButton());
                        senderService.sendMessage(chatId, CLIENT_ADDRESS, keyboard);

                        client.setState(ClientState.VOYAGE);
                        client.setFromTo("");
                        clientRepository.save(client);
                    }
                    case MAIN_MENU -> {
                        user.setUserState(UserState.USER_TYPE);
                        user.setUserRole(UserRole.SOUL);
                        userRepository.save(user);
                        senderService.askUserType(chatId, ASK_USER_TYPE);
                        return;
                    }
                    case VOYAGES -> {//TODO

                        String voyages = getAcceptedVoyages(client.getChatId());
                        client.setState(ClientState.CLIENT_MENU);
                        clientRepository.save(client);

                        senderService.deleteMessage(chatId, messageId - 1);
                        senderService.sendLongMessage(chatId, voyages);
                        senderService.sendMenu(user, CLIENT_MENU);
                    }
                    case CONNECT_ADMIN -> {
                        senderService.sendMenu(user, HELP);
                        return;
                    }
                    default -> {
                        senderService.deleteMessage(chatId, messageId);
                        senderService.sendMenu(user, WRONG_ANSWER);
                    }
                }
            }
            case VOYAGE -> {
                client.setState(ClientState.CLIENT_MENU);
                clientRepository.save(client);

                senderService.sendMenu(user, CANCELED);
                senderService.deleteMessage(chatId, messageId);
                senderService.deleteMessage(chatId, messageId - 1);
            }
        }


    }

    @Override
    @Transactional
    public void handleCallbackQuery(UserEntity user, CallbackQuery callbackQuery) {

        String userName = callbackQuery.getFrom().getUserName();
        String queryId = callbackQuery.getId();
        Integer messageId = callbackQuery.getMessage().getMessageId();

        updateUsername(user, userName);
        Long chatId = user.getChatId();

        ClientEntity client = getById(chatId);
        ClientState state = client.getState();
        if (!client.getState().equals(ClientState.VOYAGE)) {
            senderService.deleteMessage(chatId, messageId);
            senderService.sendMenu(user, CLIENT_MENU);
        }

        VoyageEntity voyage = getPeopleOrPoster(chatId);
        String data = callbackQuery.getData();
        switch (data) {
            case Components.ONE_TWO, Components.TWO_ONE -> {
                voyage.setFromTo(data);
                voyageRepository.save(voyage);
                senderService.editMessage(chatId, messageId, PEOPLE_OR_POSTAGE, getPeopleOrPoster());

            }
            case A_ONE, A_TWO, A_THREE, A_FOUR, A_POSTAGE -> {

                String voyageBuilder = "\uD83D\uDCCD " + voyage.getFromTo() +
                        "\n" +
                        data +
                        "\n" +
                        FULL_NAME +
                        "<a href=\"https://t.me/" +
                        user.getUsername() +
                        "\">" +
                        user.getFullName() +
                        "</a>" +
                        "\n" +
                        PHONE +
                         '+' + user.getPhone() +
                        "\n" +
                        Components.IS_CORRECT;


                voyage.setVoyageType(data);
                voyage.setData(voyageBuilder);
                voyageRepository.save(voyage);

                senderService.editMessage(chatId, messageId, voyageBuilder, getIsCorrect());
            }
            case YES, NO -> {

                VoyageEntity tempVoyage = getVoyage(chatId, queryId, messageId, user);

                if (data.equals(YES)) {

                    String caption = tempVoyage.getData()
                            .replace(IS_CORRECT, " ");

                    String tempVoyageId = Components.VOYAGE_ID + tempVoyage.getId();
                    caption += tempVoyageId;

                    tempVoyage.setData(caption);
                    tempVoyage.setVisibility(false);
                    tempVoyage.setVoyageState(VoyageState.ACCEPTED);
                    senderService.editMessage(chatId, messageId, caption);
                    senderService.sendMenu(user, WAIT_TAXI);
                    senderService.answerInlineButton(queryId, Components.SUCCESS_SENT);
                    client.setState(ClientState.CLIENT_MENU);
                    client.setFromTo("");
                    clientRepository.save(client);
                    voyageRepository.save(voyage);
                    senderService.handle(tempVoyage, messageId);

                } else {

                    voyage.setVoyageState(VoyageState.CANCELED);
                    voyage.setVisibility(false);

                    senderService.answerInlineButton(queryId, CANCELED);
                    senderService.sendMenu(user, CLIENT_MENU);
                    client.setState(ClientState.CLIENT_MENU);
                    client.setFromTo("");
                    clientRepository.save(client);
                    voyageRepository.save(voyage);
                }
//                senderService.deleteMessage(chatId, messageId);
//                handleMessage(user, callbackQuery.getMessage());
            }
            default -> {
                client.setState(ClientState.CLIENT_MENU);
                save(client);
                voyage.setVisibility(false);
                voyageRepository.save(voyage);
                senderService.deleteMessage(chatId, messageId);
                senderService.sendMenu(user, CANCELED);
            }
        }
    }

    @Override
    public VoyageEntity getVoyage(Long voyageId) {

        return voyageRepository
                .findFirstByIdAndVoyageStateOrderByCreatedDateDesc(voyageId, VoyageState.ACCEPTED)
                .orElseThrow();
    }

    @Override
    @Transactional
    public void deleteByChatId(Long chatId) {
        voyageRepository
                .deleteAllByClientId(chatId);
        clientRepository.deleteById(chatId);
    }

    @Override
    public void getAllVoyages(Long chatId) {
        List<VoyageEntity> all = voyageRepository.findAll();

        StringBuilder builder = new StringBuilder();
        all.stream()
                .peek(voyage -> builder.append("ID: ").append(voyage.getId()).append("\n")
                        .append("Client ID: ").append(voyage.getClientId()).append("\n")
                        .append("Voyage State: ").append(voyage.getVoyageState()).append("\n")
                        .append("From/To: ").append(voyage.getFromTo()).append("\n")
                        .append("Voyage Type: ").append(voyage.getVoyageType()).append("\n")
                        .append("Data: ").append(voyage.getData()).append("\n\n"))
                .toList();
        senderService.sendLongMessage(chatId, builder.toString());
    }

    @Override
    public List<ClientEntity> getAll() {
        return clientRepository.findAllByVisibilityTrue();
    }

    @Override
    public List<VoyageEntity> getAllVoyages() {
        return voyageRepository
                .findAll();
    }

    @Override
    public int count() {
        return clientRepository
                .countAllByVisibilityTrue();
    }

    @Override
    public long countVoyages() {
        return voyageRepository
                .count();
    }

    private VoyageEntity getVoyage(Long chatId, String queryId, Integer messageId, UserEntity user) {
        return voyageRepository
                .findFirstByClientIdAndVisibilityTrueOrderByCreatedDateDesc(chatId)
                .orElseThrow(() -> {
                    senderService.answerInlineButton(queryId, Components.EXCEPTION_OCCURED);
                    senderService.deleteMessage(chatId, messageId);
                    senderService.sendMenu(user, CLIENT_MENU);
                    return new ItemNotFoundException();
                });
    }

    private VoyageEntity getPeopleOrPoster(Long chatId) {
        return voyageRepository
                .findByClientIdAndVisibilityTrue(chatId)
                .orElseGet(() -> {
                    VoyageEntity newVoyage = VoyageEntity.builder()
                            .clientId(chatId)
                            .fromTo("")
                            .build();
                    return voyageRepository.save(newVoyage);
                });
    }

    private void updateUsername(UserEntity user, String userName) {
        user.setUsername(userName);
        userRepository.save(user);
    }

    private String getAcceptedVoyages(Long chatId) {
        StringBuilder voyages = new StringBuilder();
        voyageRepository
                .findAllByClientIdAndVoyageState(chatId, VoyageState.ACCEPTED)
                .forEach((v) -> {
                    voyages.append(v.getData())
                            .append("\n-------------------------------\n");
                });
        return voyages.toString();
    }

    private InlineKeyboardMarkup getIsCorrect() {

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton yes = senderService.getInlineButton(YES, YES);
        InlineKeyboardButton no = senderService.getInlineButton(NO, NO);
        row.add(yes);
        row.add(no);
        rows.add(row);

        return new InlineKeyboardMarkup(rows);
    }

    private InlineKeyboardMarkup getPeopleOrPoster() {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton postage = senderService.getInlineButton(A_POSTAGE, A_POSTAGE);
        row.add(postage);
        rows.add(row);
        row = new ArrayList<>();

        InlineKeyboardButton passenger1 = senderService.getInlineButton(A_ONE, A_ONE);
        InlineKeyboardButton passenger2 = senderService.getInlineButton(A_TWO, A_TWO);
        InlineKeyboardButton passenger3 = senderService.getInlineButton(A_THREE, A_THREE);
        InlineKeyboardButton passenger4 = senderService.getInlineButton(A_FOUR, A_FOUR);
        row.add(passenger1);
        row.add(passenger2);
        rows.add(row);
        row = new ArrayList<>();
        row.add(passenger3);
        row.add(passenger4);
        rows.add(row);

        return new InlineKeyboardMarkup(rows);
    }

    private InlineKeyboardMarkup getAddress() {
        return senderService.addCancelButton(senderService.getAddresses());
    }

    private ReplyKeyboardMarkup getBackButton() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(CANCEL_RIDING_BUTTON);
        rows.add(row);
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setSelective(true);
        return replyKeyboardMarkup;

    }

    private ClientEntity getById(Long chatId) {
        return clientRepository
                .findByChatIdAndVisibilityTrue(chatId).orElseGet(() -> {
                    ClientEntity build = ClientEntity.builder()
                            .chatId(chatId)
                            .state(ClientState.START
                            ).build();
                    clientRepository.save(build);

                    return build;
                });
    }

    private void save(ClientEntity client) {
        try {
            clientRepository.save(client);
        } catch (DataAccessException e) { // Catch a specific exception related to database operations
            log.error("Failed to save client: {}", e.getMessage());
            throw new RuntimeException("Failed to save client: " + e.getMessage(), e);
        }
    }

}
