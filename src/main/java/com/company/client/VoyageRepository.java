package com.company.client;

import com.company.client.components.VoyageEntity;
import com.company.client.components.VoyageState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoyageRepository extends JpaRepository<VoyageEntity, Long> {

    Optional<VoyageEntity> findFirstByClientIdAndVisibilityTrueOrderByCreatedDateDesc(Long chatId);

    Optional<VoyageEntity> findByClientIdAndVisibilityTrue(Long chatId);

    List<VoyageEntity> findAllByClientIdAndVoyageState(Long chatId, VoyageState voyageState);

    Optional<VoyageEntity> findFirstByIdAndVisibilityTrueOrderByCreatedDateDesc(Long voyageId);
    Optional<VoyageEntity> findFirstByIdAndVoyageStateOrderByCreatedDateDesc(Long voyageId, VoyageState accepted);

    void deleteAllByClientId(Long chatId);

    int countAllByVisibilityTrue();
}
