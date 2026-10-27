package com.microservice.backupserver.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(

        @NotBlank(message = "Usuário precisa ser informado.")
        String username,

        @NotBlank(message = "Senha precisa ser informada.")
        String password
) {}
