package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.gateways.DashboardGateway;
import com.microservice.backupserver.application.usecases.dto.DashboardSummaryDTO;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@RequiredArgsConstructor
public class DashboardUseCase {

    private final DashboardGateway dashboardGateway;

    public DashboardSummaryDTO getSummary(LocalDateTime startDate, LocalDateTime endDate){
        return dashboardGateway.getSummary(startDate, endDate);
    }
}
