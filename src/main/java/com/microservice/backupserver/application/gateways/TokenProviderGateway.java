package com.microservice.backupserver.application.gateways;

import com.microservice.backupserver.domain.User;

import java.util.UUID;

public interface TokenProviderGateway {

    String generateAccessToken(User user);
    String generateRefreshToken(User user);
    boolean validateRefreshToken(String token);
    UUID extractUserId(String token);
}
