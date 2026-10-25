package com.microservice.backupserver.controller;

import com.microservice.backupserver.application.usecases.BackupHistoryUseCase;
import com.microservice.backupserver.application.usecases.dto.GetBackupResponse;
import com.microservice.backupserver.controller.dto.request.CompleteBackupRequest;
import com.microservice.backupserver.controller.dto.request.CreateUploadRequest;
import com.microservice.backupserver.controller.dto.response.BackupHistoryResponse;
import com.microservice.backupserver.controller.dto.response.BackupUploadResponse;
import com.microservice.backupserver.controller.dto.response.DownloadUrlResponse;
import com.microservice.backupserver.domain.Market;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.error.Mark;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/backups")
@RequiredArgsConstructor
public class BackupController {

    private final BackupHistoryUseCase backupHistoryUseCase;

    @PostMapping("/request-upload")
    public ResponseEntity<BackupUploadResponse> requestUpload(@RequestBody @Valid CreateUploadRequest request, @AuthenticationPrincipal Market market){

        var backupHistory = backupHistoryUseCase.requestBackupUpload(market, request.fileName(), request.fileSizeBytes());
        var response = new BackupUploadResponse(backupHistory.uploadUrl(), backupHistory.backupId());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Void> completeBackup(@AuthenticationPrincipal Market market, @PathVariable UUID id, @RequestBody @Valid CompleteBackupRequest backupRequest){

        backupHistoryUseCase.completeBackup(market, id, backupRequest.status(), backupRequest.errorMessage(), backupRequest.fileHashMd5());

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}/download-url")
    public ResponseEntity<DownloadUrlResponse> requestDownload(@PathVariable UUID id){

        var response = new DownloadUrlResponse(backupHistoryUseCase.requestBackupDownload(id));

        return ResponseEntity.ok(response);
    }

    @GetMapping("/market/{marketId}")
    public ResponseEntity<Page<BackupHistoryResponse>> getBackupHistory(
            @PathVariable UUID marketId,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Pageable pageable
    ){

        GetBackupResponse useCaseResponse  = backupHistoryUseCase.getBackupHistory(marketId, startDate, endDate, pageable);

        Page<BackupHistoryResponse> response = useCaseResponse.backupHistoryPage().map(
                backupHistory -> new BackupHistoryResponse(
                        backupHistory.getId(),
                        backupHistory.getMarketId(),
                        useCaseResponse.marketName(),
                        backupHistory.getCreatedAt(),
                        backupHistory.getCompletedAt(),
                        backupHistory.getFileSizeBytes(),
                        backupHistory.getStatus(),
                        backupHistory.getErrorMessage()
                ));

        return ResponseEntity.ok(response);
    }
}
