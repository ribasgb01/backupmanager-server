package com.microservice.backupserver.infrastructure.config;

import com.microservice.backupserver.application.gateways.BackupHistoryGateway;
import com.microservice.backupserver.application.gateways.FileStorageGateway;
import com.microservice.backupserver.application.gateways.MarketGateway;
import com.microservice.backupserver.application.usecases.BackupHistoryUseCase;
import com.microservice.backupserver.application.usecases.MarketUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public BackupHistoryUseCase backupHistoryUseCase(
            BackupHistoryGateway backupHistoryGateway,
            FileStorageGateway fileStorageGateway,
            MarketGateway marketGateway
    ){
        return new BackupHistoryUseCase(backupHistoryGateway, fileStorageGateway, marketGateway);
    }

    @Bean
    public MarketUseCase marketUseCase(
            MarketGateway marketGateway
    ){
        return new MarketUseCase(marketGateway);
    }
}
