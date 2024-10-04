package com.company.auth.service;

import com.company.auth.UserRepository;
import com.company.auth.components.UserEntity;
import com.company.auth.components.UserRole;
import com.company.auth.components.UserState;
import com.company.client.service.ClientService;
import com.company.components.Components;
import com.company.expections.exp.ItemNotFoundException;
import com.company.sender.SenderService;
import com.company.taxi.service.TaxiService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    private final SenderService senderService;
    private final TaxiService taxiService;
    private final ClientService clientService;

    @PersistenceContext
    private final EntityManager entityManager;

    @Value("${admin.id}")
    private Long ADMIN_ID;

    public void handleMessage(UserEntity user, Message message) {

        Long chatId = user.getChatId();

        switch (user.getUserState()) {
            case START -> {
                user.setUserState(UserState.FULL_NAME);
                save(user);

                senderService.sendMessage(chatId, Components.ASK_FULL_NAME);
            }
            case FULL_NAME -> {
//                if (!message.hasText() || message.getText().length() < 3) {
//                    senderService.deleteMessage(chatId, message.getMessageId());
//                    senderService.sendMessage(chatId, Components.WRONG_ANSWER);
//                    return;
//                }
//                user.setFullName(message.getText().toUpperCase());
                user.setUserState(UserState.PHONE);
                save(user);
                senderService.sendMessage(chatId, Components.PL_INPUT);
                senderService.askContactRequest(chatId, Components.ASK_CONTACT_REQUEST);
            }
            case PHONE -> {
                if (!message.hasContact()) {
                    senderService.deleteMessage(chatId, message.getMessageId());
                    senderService.sendMessage(chatId, Components.WRONG_ANSWER);
                    senderService.askContactRequest(chatId, Components.ASK_CONTACT_REQUEST);
                    return;

                }
                String phone = message.getContact().getPhoneNumber();
                Long phoneId = message.getContact().getUserId();

//                if (!phone.contains("+998")) {
//                    senderService.deleteMessage(chatId, message.getMessageId());
//                    senderService.sendMessage(chatId, Components.ONLY_UZB);
//                    senderService.askContactRequest(chatId, Components.ASK_CONTACT_REQUEST);
//                    return;
//                }
                if (!phoneId.equals(chatId)) {
                    senderService.deleteMessage(chatId, message.getMessageId());
                    senderService.sendMessage(chatId, Components.NUMBER_NOT_YOURS);
                    senderService.askContactRequest(chatId, Components.ASK_CONTACT_REQUEST);
                    return;
                }
                user.setPhone(phone);
                user.setUserState(UserState.USER_TYPE);
                save(user);

                senderService.askUserType(chatId, Components.ASK_USER_TYPE);
            }

            case USER_TYPE -> {

                String userName = message.getChat().getUserName();
                if (userName != null) user.setUsername(userName);

                switch (message.getText()) {
                    case Components.TAXIST -> {
                        user.setUserRole(UserRole.TAXIST);
                        user.setUserState(UserState.REGISTRATION_DONE);
                        save(user);

//                        senderService.sendMessage(chatId, Components.SUCCESS);
                        taxiService.handleMessage(user, message);
                    }
                    case Components.CLIENT -> {
                        user.setUserRole(UserRole.CLIENT);
                        user.setUserState(UserState.REGISTRATION_DONE);
                        save(user);

                        clientService.handleMessage(user, message);
                    }
                    default -> {
                        Chat chat = message.getChat();
                        String firstName = chat.getFirstName();
                        user.setFullName(firstName);
                        user.setUsername(userName);
                        user.setUserState(UserState.USER_TYPE);
                        userRepository.save(user);

                        senderService.deleteMessage(chatId, message.getMessageId());
                        senderService.sendMessage(chatId, Components.WRONG_ANSWER);
                        senderService.askUserType(chatId, Components.ASK_USER_TYPE);
                    }
                }
            }
        }
    }


    @Override
    public UserEntity getUserById(Long chatId, String firstName) {
        return userRepository
                .findByChatIdAndVisibilityTrue(chatId)
                .orElseGet(() -> {
                            UserEntity user = UserEntity.builder()
                                    .chatId(chatId)
                                    .fullName(firstName)
                                    .userState(UserState.FULL_NAME)
                                    .userRole(UserRole.SOUL)
                                    .build();

                            return userRepository.save(user);
                        }
                );
    }

    @Override
    public List<UserEntity> getAll() {
        return userRepository.findAll();
    }

    @Override
    public void deleteByChatId(Long chatId) {
        userRepository
                .deleteById(chatId);
    }

    @Override
    @Transactional
    public void create(Long userId, String role) {
        UserEntity userById = getUserById(userId);
        userById.setUserState(UserState.FULL_NAME);
        userById.setUserRole(UserRole.ADMIN);
        save(userById);
    }

    @Override
    public int count() {
        return userRepository
                .countAllByVisibilityTrue();
    }

    @Override
    public UserEntity getUserByPhone(String userPhone) {
        return userRepository
                .findByPhone(userPhone)
                .orElseThrow(ItemNotFoundException::new);
    }

    @Override
    public int execute(String sql) {

        return entityManager
                .createNativeQuery(sql)
                .executeUpdate();
    }

    public UserEntity getUserById(Long chatId) {

        return userRepository
                .findByChatIdAndVisibilityTrue(chatId)
                .orElseGet(() -> {
                            UserEntity user = UserEntity.builder()
                                    .chatId(chatId)
                                    .userState(UserState.FULL_NAME)
                                    .userRole(UserRole.SOUL)
                                    .build();

                            return userRepository.save(user);
                        }
                );
    }

    public void save(UserEntity user) {
        try {
            userRepository.save(user);
            return;
        } catch (Exception e) {
            log.error(e.getMessage());
            senderService.sendMessage(ADMIN_ID, "```" + e.getMessage() + "```");
        }
        throw new RuntimeException();
    }

    @Override
    public void handleCallbackQuery(Long user, CallbackQuery callbackQuery) {

    }
}
