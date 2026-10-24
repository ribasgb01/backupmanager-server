package com.microservice.backupserver.application.gateways;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenGateway {

    void save(String refreshToken, UUID userId);
    Optional<UUID> findUserIdByToken(String token);
    void deleteByToken(String token);
}
