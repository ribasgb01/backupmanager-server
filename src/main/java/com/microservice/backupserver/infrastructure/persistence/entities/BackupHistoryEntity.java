package com.microservice.backupserver.infrastructure.persistence.entities;

import com.microservice.backupserver.domain.enums.BackupStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "backup_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BackupHistoryEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    private MarketEntity market;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime completedAt;
    private Long fileSizeBytes;
    private String fileHashMd5;

    @Column(nullable = false)
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BackupStatus status;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}
