package com.microservice.backupserver.infrastructure.adapters;

import com.microservice.backupserver.application.gateways.TokenStateGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class RedisTokenStateAdapter implements TokenStateGateway {

    private final StringRedisTemplate redisTemplate;
    private static final String REFRESH_TOKEN_PREFIX = "auth:refresh:";
    private static final String SESSION_PREFIX = "auth:revoked-session:";

    @Override
    public void saveRefreshToken(String refreshToken, Instant expiresAt) {

        Duration ttl = Duration.between(Instant.now(), expiresAt);
        String hashedToken = hashToken(refreshToken);
        redisTemplate.opsForValue().set(REFRESH_TOKEN_PREFIX + hashedToken, "active", ttl);
    }

    @Override
    public boolean isRefreshTokenValid(String refreshToken) {

        String hashedToken = hashToken(refreshToken);
        return Boolean.TRUE.equals(redisTemplate.hasKey(REFRESH_TOKEN_PREFIX + hashedToken));
    }

    @Override
    public boolean isSessionRevoked(UUID sessionId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(SESSION_PREFIX + sessionId));
    }

    @Override
    public void revokeSession(UUID sessionId, Instant expiresAt) {
        Duration ttl = Duration.between(Instant.now(), expiresAt);
        redisTemplate.opsForValue().set(SESSION_PREFIX + sessionId, "revoked", ttl);
    }

    @Override
    public void removeRefreshToken(String refreshToken) {
        String hashedToken = hashToken(refreshToken);
        redisTemplate.delete(REFRESH_TOKEN_PREFIX + hashedToken);
    }

    private String hashToken(String token){

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashBytes);

        } catch (NoSuchAlgorithmException e){
            throw new IllegalStateException("Algoritmo informado não foi encontrado.");
        }
    }
}
