package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.exceptions.EntityNotFoundException;
import com.microservice.backupserver.application.exceptions.InactiveUserException;
import com.microservice.backupserver.application.exceptions.InvalidTokenException;
import com.microservice.backupserver.application.exceptions.SessionRevokedException;
import com.microservice.backupserver.application.gateways.TokenProviderGateway;
import com.microservice.backupserver.application.gateways.TokenStateGateway;
import com.microservice.backupserver.application.gateways.UserGateway;
import com.microservice.backupserver.application.usecases.dto.AuthenticationResponse;
import com.microservice.backupserver.domain.User;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final TokenProviderGateway tokenProviderGateway;
    private final TokenStateGateway tokenStateGateway;
    private final UserGateway userGateway;

    public AuthenticationResponse rotateRefreshToken(String refreshToken){

        if(!tokenProviderGateway.validateRefreshToken(refreshToken)){
            throw new InvalidTokenException("Token inválido.");
        }

        if(!tokenStateGateway.isRefreshTokenValid(refreshToken)){
            throw new InvalidTokenException("Token inválido.");
        }

        UUID sessionId = tokenProviderGateway.extractSessionId(refreshToken);

        if(tokenStateGateway.isSessionRevoked(sessionId)){
            throw new SessionRevokedException("Sessão inválida.");
        }

        UUID userId = tokenProviderGateway.extractUserId(refreshToken);

        User user = userGateway.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não foi encontrado."));

        if(!user.getActive()){
            throw new InactiveUserException("Usuário não está ativo.");
        }

        String newAccessToken = tokenProviderGateway.generateAccessToken(user, sessionId);
        String newRefreshToken = tokenProviderGateway.generateRefreshToken(user, sessionId);

        Instant newRefreshTokenExpiration = tokenProviderGateway.getExpiration(newRefreshToken);

        tokenStateGateway.saveRefreshToken(newRefreshToken, newRefreshTokenExpiration);
        tokenStateGateway.removeRefreshToken(refreshToken);

        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }
}
