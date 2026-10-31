package com.microservice.backupserver.controller;

import com.microservice.backupserver.application.usecases.DashboardUseCase;
import com.microservice.backupserver.application.usecases.dto.DashboardSummaryDTO;
import com.microservice.backupserver.controller.dto.request.DashboardRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardUseCase dashboardUseCase;

    @GetMapping
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @RequestParam LocalDateTime startDate,
            @RequestParam LocalDateTime endDate){
        DashboardSummaryDTO response = dashboardUseCase.getSummary(startDate, endDate);

        return ResponseEntity.ok(response);
    }
}
