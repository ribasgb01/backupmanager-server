package com.microservice.backupserver.infrastructure.persistence;

import com.microservice.backupserver.application.usecases.dto.MarketOverviewDTO;
import com.microservice.backupserver.domain.enums.BackupStatus;
import com.microservice.backupserver.infrastructure.persistence.entities.BackupHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BackupHistoryRepository extends JpaRepository<BackupHistoryEntity, UUID> {

    Optional<BackupHistoryEntity> findFirstByMarketIdOrderByCreatedAtDesc(UUID marketId);
    Page<BackupHistoryEntity> findByMarketIdAndCreatedAtBetween(UUID marketId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    @Query("SELECT COUNT(b) FROM BackupHistoryEntity b WHERE b.createdAt BETWEEN :startDate AND :endDate")
    long countTotalBackupsByPeriod(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(b) FROM BackupHistoryEntity b WHERE b.status = :status AND b.createdAt BETWEEN :startDate AND :endDate")
    long countByStatusAndPeriod(BackupStatus status, LocalDateTime startDate, LocalDateTime endDate);
}
