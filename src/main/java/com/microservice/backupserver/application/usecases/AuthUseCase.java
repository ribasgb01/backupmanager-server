package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.exceptions.InactiveUserException;
import com.microservice.backupserver.application.exceptions.InvalidCredentialsException;
import com.microservice.backupserver.application.exceptions.RefreshTokenSaveException;
import com.microservice.backupserver.application.gateways.PasswordHasherGateway;
import com.microservice.backupserver.application.gateways.RefreshTokenGateway;
import com.microservice.backupserver.application.gateways.TokenProviderGateway;
import com.microservice.backupserver.application.gateways.UserGateway;
import com.microservice.backupserver.application.usecases.dto.AuthenticationResponse;
import com.microservice.backupserver.domain.User;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AuthUseCase {

    private final UserGateway userGateway;
    private final PasswordHasherGateway hasherGateway;
    private final TokenProviderGateway tokenProviderGateway;
    private final RefreshTokenGateway refreshTokenGateway;

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

        String accessToken = tokenProviderGateway.generateAccessToken(user);
        String refreshToken = tokenProviderGateway.generateRefreshToken(user);

        refreshTokenGateway.save(refreshToken, user.getId());

        return new AuthenticationResponse(accessToken, refreshToken);
    }
}
