package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.exceptions.*;
import com.microservice.backupserver.application.gateways.PasswordHasherGateway;
import com.microservice.backupserver.application.gateways.TokenProviderGateway;
import com.microservice.backupserver.application.gateways.TokenStateGateway;
import com.microservice.backupserver.application.gateways.UserGateway;
import com.microservice.backupserver.application.usecases.dto.AuthenticationDTO;
import com.microservice.backupserver.domain.User;
import com.microservice.backupserver.domain.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest {

    @Mock
    private UserGateway userGateway;

    @Mock
    private PasswordHasherGateway hasherGateway;

    @Mock
    private TokenProviderGateway tokenProviderGateway;

    @Mock
    private TokenStateGateway tokenStateGateway;

    @InjectMocks
    private AuthUseCase authUseCase;

    @Test
    @DisplayName("Deve lançar uma exceção quando o usuário não existe")
    void authenticateCase1() {

        String username = "inexistente";
        String rawPassword = "senha123";

        when(userGateway.findByUsername(username))
                .thenReturn(Optional.empty());

        assertThrows(
                InvalidCredentialsException.class,
                () -> authUseCase.authenticate(username, rawPassword)
        );

        verify(userGateway).findByUsername(username);

        verifyNoInteractions(
                hasherGateway,
                tokenProviderGateway,
                tokenStateGateway
        );
    }

    @Test
    @DisplayName("Deve lançar uma exceção quando o usuário não está ativo")
    void authenticateCase2(){
        String username = "user";
        String rawPassword = "rawPassword";

        User user = new User(
                UUID.fromString("eb0efcca-e58e-4c69-8dab-ec1d1d11923b"),
                username,
                "hashPassword",
                UserRole.SUPPORT,
                false,
                LocalDateTime.of(2026, 10, 28, 12, 0, 0)
        );

        when(userGateway.findByUsername(username))
                .thenReturn(Optional.of(user));

        assertThrows(
                InactiveUserException.class,
                () -> authUseCase.authenticate(username, rawPassword)
        );

        verifyNoInteractions(
                tokenProviderGateway,
                tokenStateGateway
        );
    }

    @Test
    @DisplayName("Deve lançar uma exceção quando a senha for inválida")
    void authenticateCase3(){

        String username = "username";
        String hashPassword = "hashPassword";
        String rawPassword = "rawPassword";

        User user = new User(
                UUID.fromString("eb0efcca-e58e-4c69-8dab-ec1d1d11923b"),
                username,
                hashPassword,
                UserRole.SUPPORT,
                true,
                LocalDateTime.of(2026, 10, 28, 12, 0, 0)
        );

        when(userGateway.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(hasherGateway.matches(rawPassword, hashPassword))
                .thenReturn(false);

        assertThrows(
                InvalidCredentialsException.class,
                () -> authUseCase.authenticate(username, rawPassword)
        );

        verifyNoInteractions(tokenStateGateway, tokenProviderGateway);
    }

    @Test
    @DisplayName("Deve autenticar quando as credenciais forem válidas")
    void authenticateCase4(){

        String username = "username";
        String hashPassword = "hashPassword";
        String rawPassword = "rawPassword";

        User user = new User(
                UUID.fromString("eb0efcca-e58e-4c69-8dab-ec1d1d11923b"),
                username,
                hashPassword,
                UserRole.SUPPORT,
                true,
                LocalDateTime.of(2026, 10, 28, 12, 0, 0)
        );

        when(userGateway.findByUsername(username))
                .thenReturn(Optional.of(user));

        when(hasherGateway.matches(any(), any()))
                .thenReturn(true);

        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        when(tokenProviderGateway.generateAccessToken(eq(user), any(UUID.class)))
                .thenReturn(accessToken);

        when(tokenProviderGateway.generateRefreshToken(eq(user), any(UUID.class)))
                .thenReturn(refreshToken);

        Instant accessTokenExpiration = Instant.parse("2026-10-28T18:00:00Z");
        Instant refreshTokenExpiration = Instant.parse("2026-11-04T18:00:00Z");

        when(tokenProviderGateway.getExpiration(accessToken))
                .thenReturn(accessTokenExpiration);

        when(tokenProviderGateway.getExpiration(refreshToken))
                .thenReturn(refreshTokenExpiration);

        AuthenticationDTO response = authUseCase.authenticate(username, rawPassword);

        verify(tokenStateGateway, times(1))
                .saveRefreshToken(refreshToken, refreshTokenExpiration);

        assertEquals(accessToken, response.accessToken());
        assertEquals(accessTokenExpiration, response.accessExpiration());
        assertEquals(refreshToken, response.refreshToken());
        assertEquals(refreshTokenExpiration, response.refreshExpiration());

    }

    @Test
    @DisplayName("Deve retornar uma exceção se o token não tiver uma estrutura válida")
    void logoutCase1(){

        String refreshToken = "refreshToken";

        when(tokenProviderGateway.validateRefreshToken(refreshToken))
                .thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> authUseCase.logout(refreshToken));

        verifyNoInteractions(tokenStateGateway);
    }

    @Test
    @DisplayName("Deve retornar uma exceção se o token for inválido no state")
    void logoutCase2(){

        String refreshToken = "refreshToken";

        when(tokenProviderGateway.validateRefreshToken(refreshToken))
                .thenReturn(true);

        when(tokenStateGateway.isRefreshTokenValid(refreshToken))
                .thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> authUseCase.logout(refreshToken));

        verify(tokenStateGateway, never()).revokeSession(any(), any());
    }

    @Test
    @DisplayName("Deve realizar o logout se o refresh token for válido")
    void logoutCase3(){

        String refreshToken = "refreshToken";

        when(tokenProviderGateway.validateRefreshToken(refreshToken))
                .thenReturn(true);

        when(tokenStateGateway.isRefreshTokenValid(refreshToken))
                .thenReturn(true);

        UUID sessionId = UUID.fromString("eb0efcca-e58e-4c69-8dab-ec1d1d11923b");

        when(tokenProviderGateway.extractSessionId(refreshToken))
                .thenReturn(sessionId);

        Instant refreshTokenExpiration = Instant.parse("2026-10-28T18:00:00Z");

        when(tokenProviderGateway.getExpiration(refreshToken))
                .thenReturn(refreshTokenExpiration);

        authUseCase.logout(refreshToken);

        verify(tokenStateGateway, times(1))
                .revokeSession(sessionId, refreshTokenExpiration);

        verify(tokenStateGateway, times(1))
                .removeRefreshToken(refreshToken);
    }

    @Test
    @DisplayName("Deve lançar exceção ao rotacionar token com estrutura inválida")
    void rotateRefreshTokenCase1() {

        when(tokenProviderGateway.validateRefreshToken("bad-token"))
                .thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> authUseCase.rotateRefreshToken("bad-token"));

        verifyNoInteractions(tokenStateGateway, userGateway);
    }

    @Test
    @DisplayName("Deve lançar exceção ao rotacionar token ausente do state")
    void rotateRefreshTokenCase2() {

        when(tokenProviderGateway.validateRefreshToken("refresh"))
                .thenReturn(true);

        when(tokenStateGateway.isRefreshTokenValid("refresh"))
                .thenReturn(false);

        assertThrows(InvalidTokenException.class,
                () -> authUseCase.rotateRefreshToken("refresh"));

        verify(tokenStateGateway, never()).isSessionRevoked(any());

        verifyNoInteractions(userGateway);
    }

    @Test
    @DisplayName("Deve lançar exceção quando a sessão estiver revogada")
    void rotateRefreshTokenCase3() {
        when(tokenProviderGateway.validateRefreshToken("refresh")).thenReturn(true);
        when(tokenStateGateway.isRefreshTokenValid("refresh")).thenReturn(true);
        UUID sessionId = UUID.randomUUID();
        when(tokenProviderGateway.extractSessionId("refresh")).thenReturn(sessionId);
        when(tokenStateGateway.isSessionRevoked(sessionId)).thenReturn(true);
        assertThrows(SessionRevokedException.class, () -> authUseCase.rotateRefreshToken("refresh"));
        verifyNoInteractions(userGateway);
        verify(tokenStateGateway, never()).removeRefreshToken(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o usuário do refresh token não existir")
    void rotateRefreshTokenCase4() {
        when(tokenProviderGateway.validateRefreshToken("refresh"))
                .thenReturn(true);

        when(tokenStateGateway.isRefreshTokenValid("refresh"))
                .thenReturn(true);

        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(tokenProviderGateway.extractSessionId("refresh"))
                .thenReturn(sessionId);

        when(tokenStateGateway.isSessionRevoked(sessionId))
                .thenReturn(false);

        when(tokenProviderGateway.extractUserId("refresh"))
                .thenReturn(userId);

        when(userGateway.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> authUseCase.rotateRefreshToken("refresh"));

        verify(tokenStateGateway, never())
                .removeRefreshToken(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário do refresh token estiver inativo")
    void rotateRefreshTokenCase5() {

        when(tokenProviderGateway.validateRefreshToken("refresh"))
                .thenReturn(true);

        when(tokenStateGateway.isRefreshTokenValid("refresh"))
                .thenReturn(true);

        UUID sessionId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(tokenProviderGateway.extractSessionId("refresh"))
                .thenReturn(sessionId);

        when(tokenStateGateway.isSessionRevoked(sessionId))
                .thenReturn(false);

        when(tokenProviderGateway.extractUserId("refresh"))
                .thenReturn(userId);

        User user = new User(
                userId,
                "user",
                "hash",
                UserRole.SUPPORT,
                false,
                LocalDateTime.of(2026, 10, 28, 12, 0)
        );

        when(userGateway.findById(userId))
                .thenReturn(Optional.of(user));

        assertThrows(InactiveUserException.class,
                () -> authUseCase.rotateRefreshToken("refresh"));

        verify(tokenStateGateway, never()).removeRefreshToken(any());
    }

    @Test
    @DisplayName("Deve rotacionar refresh token válido e persistir o novo token")
    void rotateRefreshTokenCase6() {

        when(tokenProviderGateway.validateRefreshToken("old-refresh"))
                .thenReturn(true);

        when(tokenStateGateway.isRefreshTokenValid("old-refresh"))
                .thenReturn(true);

        UUID sessionId = UUID.randomUUID(), userId = UUID.randomUUID();

        when(tokenProviderGateway.extractSessionId("old-refresh"))
                .thenReturn(sessionId);

        when(tokenStateGateway.isSessionRevoked(sessionId))
                .thenReturn(false);

        when(tokenProviderGateway.extractUserId("old-refresh"))
                .thenReturn(userId);

        User user = new User(
                userId,
                "user",
                "hash",
                UserRole.SUPPORT,
                true,
                LocalDateTime.of(2026, 10, 28, 12, 0)
        );

        when(userGateway.findById(userId))
                .thenReturn(Optional.of(user));

        when(tokenProviderGateway.generateAccessToken(user, sessionId))
                .thenReturn("new-access");

        when(tokenProviderGateway.generateRefreshToken(user, sessionId))
                .thenReturn("new-refresh");

        Instant accessExpiration = Instant.parse("2026-10-28T18:00:00Z");
        Instant refreshExpiration = Instant.parse("2026-11-04T18:00:00Z");

        when(tokenProviderGateway.getExpiration("new-access"))
                .thenReturn(accessExpiration);

        when(tokenProviderGateway.getExpiration("new-refresh"))
                .thenReturn(refreshExpiration);

        AuthenticationDTO response = authUseCase.rotateRefreshToken("old-refresh");

        assertEquals("new-access", response.accessToken());
        assertEquals(accessExpiration, response.accessExpiration());
        assertEquals("new-refresh", response.refreshToken());
        assertEquals(refreshExpiration, response.refreshExpiration());

        verify(tokenStateGateway).saveRefreshToken("new-refresh", refreshExpiration);
        verify(tokenStateGateway).removeRefreshToken("old-refresh");
    }

}
