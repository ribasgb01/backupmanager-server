package com.microservice.backupserver.controller.dto.response;

import java.util.UUID;

public record BackupUploadResponse(
        String uploadUrl,
        UUID backupId
) {
}
