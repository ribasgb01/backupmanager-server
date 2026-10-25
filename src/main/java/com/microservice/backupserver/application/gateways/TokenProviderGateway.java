package com.microservice.backupserver.application.gateways;

import com.microservice.backupserver.domain.User;

import java.time.Instant;
import java.util.UUID;

public interface TokenProviderGateway {

    String generateAccessToken(User user, UUID sessionId);
    String generateRefreshToken(User user, UUID sessionId);
    Instant getExpiration(String token);
    boolean validateRefreshToken(String token);
    UUID extractUserId(String token);
    UUID extractSessionId(String token);
}
