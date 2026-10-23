package com.microservice.backupserver.application.gateways;

import com.microservice.backupserver.domain.BackupHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface BackupHistoryGateway {

    BackupHistory save(BackupHistory backupHistory);
    Optional<BackupHistory> findById(UUID id);
    Optional<BackupHistory> findLatestByMarketId(UUID marketId);
    Page<BackupHistory> findByPeriod (UUID marketId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
