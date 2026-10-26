package com.microservice.backupserver.application.gateways;

import java.time.Instant;
import java.util.UUID;

public interface TokenStateGateway {

    void saveRefreshToken(String refreshToken, Instant expiresAt);
    boolean isRefreshTokenValid(String refreshToken);
    boolean isSessionRevoked(UUID sessionId);
    void revokeSession(UUID sessionId, Instant expiresAt);
    void removeRefreshToken(String refreshToken);
}
