package com.microservice.backupserver.application.gateways;

import com.microservice.backupserver.application.usecases.dto.DashboardSummaryDTO;

import java.time.LocalDateTime;

public interface DashboardGateway {

    DashboardSummaryDTO getSummary(LocalDateTime start, LocalDateTime end);
}
