package com.microservice.backupserver.controller.dto.request;

public record LoginRequest(
        String username,
        String password
) {}
