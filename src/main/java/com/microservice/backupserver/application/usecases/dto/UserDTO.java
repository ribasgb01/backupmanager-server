package com.microservice.backupserver.application.usecases.dto;

import com.microservice.backupserver.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserDTO(
        String id,
        String username,
        String role,
        long createdAt
) {
}
