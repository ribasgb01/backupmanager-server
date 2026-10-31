package com.microservice.backupserver.infrastructure.config;

import com.microservice.backupserver.application.gateways.*;
import com.microservice.backupserver.application.usecases.*;
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

    @Bean
    public AuthUseCase authUseCase(
        UserGateway userGateway,
        PasswordHasherGateway hasherGateway,
        TokenProviderGateway tokenProviderGateway,
        TokenStateGateway tokenStateGateway
    ){
        return new AuthUseCase(userGateway, hasherGateway, tokenProviderGateway, tokenStateGateway);
    }

    @Bean
    public DashboardUseCase dashboardUseCase(
            DashboardGateway dashboardGateway
    ){
        return new DashboardUseCase(dashboardGateway);
    }

    @Bean
    public UserUseCase userUseCase(
            PasswordHasherGateway hasherGateway,
            UserGateway userGateway
    ){
        return new UserUseCase(hasherGateway, userGateway);
    }
}
