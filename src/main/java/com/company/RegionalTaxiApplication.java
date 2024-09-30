package com.company;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class RegionalTaxiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegionalTaxiApplication.class, args);

        
    }
}