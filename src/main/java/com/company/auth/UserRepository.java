package com.company.auth;

import com.company.auth.components.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByChatIdAndVisibilityTrue(Long chatId);
    int countAllByVisibilityTrue();

    Optional<UserEntity> findByPhone(String userPhone);
}