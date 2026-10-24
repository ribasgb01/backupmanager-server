package com.microservice.backupserver.application.usecases.dto;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {}
