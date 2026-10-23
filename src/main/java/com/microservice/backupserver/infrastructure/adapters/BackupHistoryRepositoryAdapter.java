package com.microservice.backupserver.infrastructure.adapters;

import com.microservice.backupserver.application.gateways.BackupHistoryGateway;
import com.microservice.backupserver.domain.BackupHistory;
import com.microservice.backupserver.infrastructure.persistence.BackupHistoryRepository;
import com.microservice.backupserver.infrastructure.persistence.mappers.BackupHistoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BackupHistoryRepositoryAdapter implements BackupHistoryGateway {

    private final BackupHistoryMapper backupHistoryMapper;
    private final BackupHistoryRepository backupHistoryRepository;

    @Override
    public BackupHistory save(BackupHistory backupHistory) {
        return backupHistoryMapper.toDomain(
                backupHistoryRepository.save(
                        backupHistoryMapper.toEntity(backupHistory)
                )
        );
    }

    @Override
    public Optional<BackupHistory> findById(UUID id) {
        return backupHistoryRepository.findById(id)
                .map(backupHistoryMapper::toDomain);
    }

    @Override
    public Optional<BackupHistory> findLatestByMarketId(UUID marketId) {
        return backupHistoryRepository.findFirstByMarketIdOrderByCreatedAtDesc(marketId)
                .map(backupHistoryMapper::toDomain);
    }

    @Override
    public Page<BackupHistory> findByPeriod(UUID marketId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return backupHistoryRepository.findByMarketIdAndCreatedAtBetween(marketId, startDate, endDate, pageable)
                .map(backupHistoryMapper::toDomain);
    }
}
