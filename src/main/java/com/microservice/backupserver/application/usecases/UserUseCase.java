package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.exceptions.UserAlreadyExistsException;
import com.microservice.backupserver.application.gateways.PasswordHasherGateway;
import com.microservice.backupserver.application.gateways.UserGateway;
import com.microservice.backupserver.domain.User;
import com.microservice.backupserver.domain.enums.UserRole;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class UserUseCase {

    private final PasswordHasherGateway hasherGateway;
    private final UserGateway userGateway;

    public User createUser(String username, String rawPassword){

        if (userGateway.existsByUsername(username)){
            throw new UserAlreadyExistsException("Já existe um usuário cadastrado com esse nome.");
        }

        String hashedPassword = hasherGateway.encode(rawPassword);

        User user = User.builder()
                .id(UUID.randomUUID())
                .username(username)
                .passwordHash(hashedPassword)
                .role(UserRole.SUPPORT)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();

        return userGateway.save(user);
    }
}
