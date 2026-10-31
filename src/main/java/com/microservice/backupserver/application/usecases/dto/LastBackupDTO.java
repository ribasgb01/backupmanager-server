package com.microservice.backupserver.application.usecases.dto;

import com.microservice.backupserver.domain.enums.BackupStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record LastBackupDTO(
    UUID id,
    LocalDateTime createdAt,
    LocalDateTime completedAt,
    String status,
    Long fileSizeBytes
) {
}
