package com.microservice.backupserver.controller.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken
) {
}
