package com.microservice.backupserver.application.usecases.dto;

import java.util.UUID;

public record BackupUploadDTO(
        UUID backupId,
        String uploadUrl
) {
}
