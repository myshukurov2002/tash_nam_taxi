package com.company.config;

import com.company.admin.AdminComponents;
import com.company.admin.AdminService;
import com.company.sender.SenderService;
import com.company.taxi.components.TaxiEntity;
import com.company.taxi.service.TaxiService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

import static com.company.components.Components.ADMIN_ID;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class AppConfig {

    private final TaxiService taxiService;
    private final SenderService senderService;
    private final AdminService adminService;


    @Scheduled(cron = "0 59 23 * * ?")
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
                adminService.banTaxi(taxi.getChatId());
                taxi.setStatus(false);
                taxiService.save(taxi);
                senderService.sendMessage(ADMIN_ID, "banned taxi ID: " + taxi.getChatId());
            }
            senderService.sendMessage(ADMIN_ID, "autoban worked");
        }
    }

}
