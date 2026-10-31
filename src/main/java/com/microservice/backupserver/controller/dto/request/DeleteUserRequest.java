package com.microservice.backupserver.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DeleteUserRequest (

        @NotNull(message = "ID do usuário é obrigatório.")
        UUID userId
){}
