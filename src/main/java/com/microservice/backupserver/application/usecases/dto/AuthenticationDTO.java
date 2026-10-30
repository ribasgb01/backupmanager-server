package com.microservice.backupserver.application.usecases.dto;

import java.time.Instant;

public record AuthenticationDTO(
        String accessToken,
        Instant accessExpiration,
        String refreshToken,
        Instant refreshExpiration
) {}
