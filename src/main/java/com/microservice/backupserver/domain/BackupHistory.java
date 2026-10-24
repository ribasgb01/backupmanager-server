package com.microservice.backupserver.domain;

import com.microservice.backupserver.domain.enums.BackupStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BackupHistory {
    private UUID id;
    private UUID marketId;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long fileSizeBytes;
    private String fileHashMd5;
    private String s3Key;
    private BackupStatus status;
    private String errorMessage;
}
