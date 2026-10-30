package com.microservice.backupserver.controller.dto.response;

import com.microservice.backupserver.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        UserRole role,
        Boolean active,
        LocalDateTime createdAt
){}
