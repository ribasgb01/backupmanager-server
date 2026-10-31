package com.microservice.backupserver.application.usecases.dto;

import com.microservice.backupserver.domain.Market;

import java.time.LocalTime;
import java.util.UUID;

public record MarketOverviewDTO(
        UUID marketId,
        String marketName,
        String cnpj,
        LocalTime expectedBackupTime,
        LastBackupDTO lastBackup
) { }
