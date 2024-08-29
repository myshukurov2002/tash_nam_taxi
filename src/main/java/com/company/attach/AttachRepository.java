package com.company.attach;

import com.company.attach.components.AttachEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AttachRepository extends JpaRepository<AttachEntity, String> {
    Optional<AttachEntity> findByChatIdAndVisibilityTrue(Long chatId);
}
