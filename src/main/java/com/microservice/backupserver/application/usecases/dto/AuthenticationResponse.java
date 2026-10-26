package com.microservice.backupserver.application.usecases.dto;

import java.time.Instant;

public record AuthenticationResponse(
        String accessToken,
        Instant accessExpiration,
        String refreshToken,
        Instant refreshExpiration
) {}
