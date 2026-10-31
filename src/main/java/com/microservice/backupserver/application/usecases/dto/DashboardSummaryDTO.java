package com.microservice.backupserver.application.usecases.dto;

public record DashboardSummaryDTO(

        long activeMarkets,
        long totalBackups,
        long successfulBackups,
        long failedBackups,
        long pendingBackups
) {
}
