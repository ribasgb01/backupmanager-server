package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.exceptions.UserAlreadyExistsException;
import com.microservice.backupserver.application.gateways.PasswordHasherGateway;
import com.microservice.backupserver.application.gateways.UserGateway;
import com.microservice.backupserver.domain.User;
import com.microservice.backupserver.domain.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private PasswordHasherGateway hasherGateway;

    @Mock
    private UserGateway userGateway;

    @InjectMocks
    private UserUseCase userUseCase;

    @Test
    @DisplayName("Deve lançar exceção quando o username já existir")
    void createUserCase1() {

        when(userGateway.existsByUsername("user"))
                .thenReturn(true);

        assertThrows(UserAlreadyExistsException.class,
                () -> userUseCase.createUser("user", "senha"));

        verify(userGateway).existsByUsername("user");
        verifyNoInteractions(hasherGateway);
        verify(userGateway, never()).save(any());
    }

    @Test
    @DisplayName("Deve criar usuário com senha criptografada e papel SUPPORT")
    void createUserCase2() {

        when(userGateway.existsByUsername("user"))
                .thenReturn(false);

        when(hasherGateway.encode("senha"))
                .thenReturn("senha-hash");

        when(userGateway.save(any(User.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        User user = userUseCase.createUser("user", "senha");

        assertNotNull(user.getId());
        assertEquals("user", user.getUsername());
        assertEquals("senha-hash", user.getPasswordHash());
        assertEquals(UserRole.SUPPORT, user.getRole());

        assertTrue(user.getActive());
        assertNotNull(user.getCreatedAt());

        verify(hasherGateway).encode("senha");

        verify(userGateway).save(user);
    }
}
