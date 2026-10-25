package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.exceptions.BusinessRuleException;
import com.microservice.backupserver.application.exceptions.InactiveUserException;
import com.microservice.backupserver.application.exceptions.InvalidCredentialsException;
import com.microservice.backupserver.application.exceptions.InvalidTokenException;
import com.microservice.backupserver.application.gateways.PasswordHasherGateway;
import com.microservice.backupserver.application.gateways.TokenStateGateway;
import com.microservice.backupserver.application.gateways.TokenProviderGateway;
import com.microservice.backupserver.application.gateways.UserGateway;
import com.microservice.backupserver.application.usecases.dto.AuthenticationResponse;
import com.microservice.backupserver.domain.User;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class AuthUseCase {

    private final UserGateway userGateway;
    private final PasswordHasherGateway hasherGateway;
    private final TokenProviderGateway tokenProviderGateway;
    private final TokenStateGateway tokenStateGateway;

    public AuthenticationResponse authenticate(String username, String rawPassword){

        User user = userGateway.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Usuário ou senha incorretos."));

        boolean isPasswordValid = hasherGateway.matches(rawPassword, user.getPasswordHash());

        if(!user.getActive()){
            throw new InactiveUserException("Usuário não está ativo.");
        }

        if(!isPasswordValid){
            throw new InvalidCredentialsException("Usuário ou senha incorretos.");
        }

        UUID sessionId = UUID.randomUUID();

        String accessToken = tokenProviderGateway.generateAccessToken(user, sessionId);
        String refreshToken = tokenProviderGateway.generateRefreshToken(user, sessionId);

        Instant refreshTokenExpiration = tokenProviderGateway.getExpiration(refreshToken);

        tokenStateGateway.saveRefreshToken(refreshToken, refreshTokenExpiration);

        return new AuthenticationResponse(accessToken, refreshToken);
    }

    public void logout(String refreshToken){

        if(!tokenProviderGateway.validateRefreshToken(refreshToken)){
            throw new InvalidTokenException("Token inválido.");
        }

        if(!tokenStateGateway.isRefreshTokenValid(refreshToken)){
            throw new InvalidTokenException("Token inválido.");
        }

        UUID sessionId = tokenProviderGateway.extractSessionId(refreshToken);

        Instant refreshTokenExpiration = tokenProviderGateway.getExpiration(refreshToken);

        tokenStateGateway.revokeSession(sessionId, refreshTokenExpiration);
        tokenStateGateway.removeRefreshToken(refreshToken);
    }
}
