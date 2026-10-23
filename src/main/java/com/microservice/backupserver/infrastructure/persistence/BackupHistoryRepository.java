package com.microservice.backupserver.infrastructure.persistence;

import com.microservice.backupserver.infrastructure.persistence.entities.BackupHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface BackupHistoryRepository extends JpaRepository<BackupHistoryEntity, UUID> {

    Optional<BackupHistoryEntity> findFirstByMarketIdOrderByCreatedAtDesc(UUID marketId);
    Page<BackupHistoryEntity> findByMarketIdAndCreatedAtBetween(UUID marketId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
