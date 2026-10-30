package com.microservice.backupserver.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record DeleteUserRequest (

        @NotBlank(message = "ID do usuário é obrigatório.")
        UUID userId
){}
