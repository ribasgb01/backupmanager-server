package com.microservice.backupserver.infrastructure.adapters;

import com.microservice.backupserver.application.gateways.DashboardGateway;
import com.microservice.backupserver.application.usecases.dto.DashboardSummaryDTO;
import com.microservice.backupserver.domain.enums.BackupStatus;
import com.microservice.backupserver.infrastructure.persistence.BackupHistoryRepository;
import com.microservice.backupserver.infrastructure.persistence.MarketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DashboardAdapter implements DashboardGateway {

    private final MarketRepository marketRepository;
    private final BackupHistoryRepository backupHistoryRepository;


    @Override
    public DashboardSummaryDTO getSummary(LocalDateTime startDate, LocalDateTime endDate) {

        return new DashboardSummaryDTO(
                marketRepository.countActiveMarkets(),
                backupHistoryRepository.countTotalBackupsByPeriod(startDate, endDate),
                backupHistoryRepository.countByStatusAndPeriod(BackupStatus.SUCCESS, startDate, endDate),
                backupHistoryRepository.countByStatusAndPeriod(BackupStatus.FAILED, startDate, endDate),
                backupHistoryRepository.countByStatusAndPeriod(BackupStatus.PENDING, startDate, endDate)
                );
    }
}
