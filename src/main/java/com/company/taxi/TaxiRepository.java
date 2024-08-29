package com.company.taxi;

import com.company.taxi.components.TaxiEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaxiRepository extends JpaRepository<TaxiEntity, Long> {
    Optional<TaxiEntity> findByChatIdAndVisibilityTrue(Long chatId);

    List<TaxiEntity> findAllByStatusTrue();

}
