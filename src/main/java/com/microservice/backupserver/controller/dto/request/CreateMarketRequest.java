package com.microservice.backupserver.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record CreateMarketRequest(

        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "O CNPJ é obrigatório")
        String cnpj,

        @NotNull(message = "Horário esperado de backup é obrigatório.")
        LocalTime expectedBackupTime
) {}
