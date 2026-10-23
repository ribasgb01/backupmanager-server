package com.microservice.backupserver.application.usecases.dto;

import java.util.UUID;

public record BackupUploadResponse(
        UUID backupId,
        String uploadUrl
) {
}
