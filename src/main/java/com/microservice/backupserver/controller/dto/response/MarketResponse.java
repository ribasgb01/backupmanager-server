package com.microservice.backupserver.controller.dto.response;

import java.time.LocalTime;
import java.util.UUID;

public record MarketResponse(
        UUID id,
        String name,
        String cnpj,
        String apiKey,
        LocalTime expectedBackupTime,
        Boolean active
) {}
