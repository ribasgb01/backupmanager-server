package com.microservice.backupserver.controller.dto.request;

import com.microservice.backupserver.domain.enums.BackupStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CompleteBackupRequest(

        @NotNull(message = "Status não foi informado.")
        BackupStatus status,

        @NotBlank(message = "Hash MD5 não foi informado.")
        String fileHashMd5,

        String errorMessage
) {
}
