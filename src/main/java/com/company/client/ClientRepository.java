package com.company.client;

import com.company.client.components.ClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClientRepository extends JpaRepository<ClientEntity, Long> {
    Optional<ClientEntity> findByChatIdAndVisibilityTrue(Long chatId);

    List<ClientEntity> findAllByVisibilityTrue();

    int countAllByVisibilityTrue();
}
