package com.microservice.backupserver.application.usecases.dto;

import com.microservice.backupserver.domain.BackupHistory;
import org.springframework.data.domain.Page;

public record GetBackupResponse(
        Page<BackupHistory> backupHistoryPage,
        String marketName
) {
}
