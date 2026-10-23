package com.microservice.backupserver.controller.dto.response;

import com.microservice.backupserver.domain.enums.BackupStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record BackupHistoryResponse(

        UUID id,
        UUID marketId,
        String marketName,
        LocalDateTime createdAt,
        LocalDateTime completedAt,
        Long fileSizeBytes,
        BackupStatus status,
        String errorMessage
) {}
