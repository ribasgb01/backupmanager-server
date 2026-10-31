package com.microservice.backupserver.controller.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record DashboardRequest (

        @NotNull(message = "Data inicial é obrigatória.")
        LocalDateTime startDate,

        @NotNull(message = "Data final é obrigatória.")
        LocalDateTime endDate
){}
