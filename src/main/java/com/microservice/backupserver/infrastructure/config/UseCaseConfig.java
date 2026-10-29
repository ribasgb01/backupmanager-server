package com.microservice.backupserver.infrastructure.config;

import com.microservice.backupserver.application.gateways.*;
import com.microservice.backupserver.application.usecases.AuthUseCase;
import com.microservice.backupserver.application.usecases.BackupHistoryUseCase;
import com.microservice.backupserver.application.usecases.MarketUseCase;
import com.microservice.backupserver.application.usecases.UserUseCase;
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
    public UserUseCase userUseCase(
            PasswordHasherGateway hasherGateway,
            UserGateway userGateway
    ){
        return new UserUseCase(hasherGateway, userGateway);
    }
}
