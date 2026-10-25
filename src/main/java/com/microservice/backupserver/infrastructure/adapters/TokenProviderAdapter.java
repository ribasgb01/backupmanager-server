package com.microservice.backupserver.infrastructure.adapters;

import com.microservice.backupserver.application.exceptions.InvalidTokenException;
import com.microservice.backupserver.application.gateways.TokenProviderGateway;
import com.microservice.backupserver.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class TokenProviderAdapter implements TokenProviderGateway {

    @Value("${api.security.token.secret}")
    private String secret;

    @Value("${api.security.token.refresh-expiration}")
    private Long refreshExpirationTime;

    @Value("${api.security.token.access-expiration}")
    private Long accessExpirationTime;

    @Override
    public String generateAccessToken(User user, UUID sessionId) {

        SecretKey key = getSigningKey();
        Date expiryDate = Date.from(Instant.now().plusMillis(accessExpirationTime));

        return Jwts.builder()
                .subject(user.getId().toString())
                .expiration(expiryDate)
                .claim("sid", sessionId.toString())
                .claim("type", "access")
                .signWith(key)
                .compact();
    }

    @Override
    public String generateRefreshToken(User user, UUID sessionId) {

        SecretKey key = getSigningKey();
        Date expiryDate = Date.from(Instant.now().plusMillis(refreshExpirationTime));

        return Jwts.builder()
                .subject(user.getId().toString())
                .expiration(expiryDate)
                .claim("sid", sessionId.toString())
                .claim("type", "refresh")
                .signWith(key)
                .compact();
    }

    @Override
    public Instant getExpiration(String token) {

        try {
            SecretKey key = getSigningKey();

            Date expiryDate = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getExpiration();

            return expiryDate.toInstant();

        } catch (JwtException | IllegalArgumentException e){
                throw new InvalidTokenException("Token inválido.");
        }
    }

    @Override
    public boolean validateRefreshToken(String token) {

        try {
            SecretKey key = getSigningKey();

            Claims payload = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String type = payload.get("type", String.class);

            if(!type.equals("refresh")){
                return false;
            }

        } catch (Exception e){
            return false;
        }

        return true;
    }

    @Override
    public UUID extractUserId(String token) {

        try {
            SecretKey key = getSigningKey();

            String userId = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject();

            return UUID.fromString(userId);

        } catch (JwtException | IllegalArgumentException e){
            throw new InvalidTokenException("Token inválido.");
        }
    }

    @Override
    public UUID extractSessionId(String token) {

        try {
            SecretKey key = getSigningKey();

            String sessionId = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .get("sid", String.class);

            return UUID.fromString(sessionId);

        } catch (JwtException | IllegalArgumentException e){
            throw new InvalidTokenException("Token inválido.");
        }
    }

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

}
