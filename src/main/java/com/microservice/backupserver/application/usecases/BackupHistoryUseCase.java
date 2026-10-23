package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.exceptions.BusinessRuleException;
import com.microservice.backupserver.application.exceptions.EntityNotFoundException;
import com.microservice.backupserver.application.gateways.BackupHistoryGateway;
import com.microservice.backupserver.application.gateways.FileStorageGateway;
import com.microservice.backupserver.application.gateways.MarketGateway;
import com.microservice.backupserver.application.usecases.dto.BackupUploadResponse;
import com.microservice.backupserver.application.usecases.dto.GetBackupResponse;
import com.microservice.backupserver.domain.BackupHistory;
import com.microservice.backupserver.domain.Market;
import com.microservice.backupserver.domain.enums.BackupStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
public class BackupHistoryUseCase {

    private final BackupHistoryGateway backupHistoryGateway;
    private final FileStorageGateway fileStorageGateway;
    private final MarketGateway marketGateway;

    private static final Duration BACKUP_TOLERANCE_TIME = Duration.ofHours(2);
    private static final Duration UPLOAD_URL_EXPIRATION = Duration.ofMinutes(30);
    private static final Duration DOWNLOAD_URL_EXPIRATION = Duration.ofMinutes(15);

    public BackupUploadResponse requestBackupUpload(Market market, String fileName, Long fileSizeBytes){

        LocalDateTime currentDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDate = currentDate.format(formatter);

        String fileStoragePath = String.format(
                "markets/%s/backups/%d/%02d/%s_%s",
                market.getId(),
                currentDate.getYear(),
                currentDate.getMonthValue(),
                formattedDate,
                fileName
        );

        String presignedUrl = fileStorageGateway.generateUploadPresignedUrl(fileStoragePath, UPLOAD_URL_EXPIRATION);

        BackupHistory backupHistory = BackupHistory.builder()
                .id(UUID.randomUUID())
                .marketId(market.getId())
                .createdAt(currentDate)
                .completedAt(null)
                .fileSizeBytes(fileSizeBytes)
                .fileHashMd5(null)
                .s3Key(fileStoragePath)
                .status(BackupStatus.PENDING)
                .errorMessage(null)
                .build();

        BackupHistory savedBackupHistory = backupHistoryGateway.save(backupHistory);

        return new BackupUploadResponse(savedBackupHistory.getId(), presignedUrl);
    }

    public void completeBackup(UUID backupId, BackupStatus status, String errorMessage, String fileHashMd5){

        BackupHistory backupHistory = backupHistoryGateway.findById(backupId)
                .orElseThrow(() -> new EntityNotFoundException("Este backup não foi encontrado."));

        if(backupHistory.getStatus() != BackupStatus.PENDING){
            throw new BusinessRuleException("Este backup não está constando como pendente.");
        }

        if (status == BackupStatus.FAILED){
            if (errorMessage == null){
                throw new IllegalArgumentException("A mensagem de erro é obrigatória quando o status for FAILED.");
            }
        }

        backupHistory.setStatus(status);
        backupHistory.setErrorMessage(errorMessage);
        backupHistory.setFileHashMd5(fileHashMd5);
        backupHistory.setCompletedAt(LocalDateTime.now());

        backupHistoryGateway.save(backupHistory);
    }

    public String requestBackupDownload(UUID backupId){

        BackupHistory backupHistory = backupHistoryGateway.findById(backupId)
                .orElseThrow(() -> new EntityNotFoundException("Este backup não foi encontrado."));

        if (backupHistory.getStatus() != BackupStatus.SUCCESS){
            throw new BusinessRuleException("Este backup não foi gerado com sucesso, não é possível recuperar.");
        }

        return fileStorageGateway.generateDownloadPresignedUrl(backupHistory.getS3Key(), DOWNLOAD_URL_EXPIRATION);
    }

    public GetBackupResponse getBackupHistory(UUID marketId, LocalDate startDate, LocalDate endDate, Pageable pageable){

        Market market = marketGateway.findById(marketId)
                .orElseThrow(() -> new EntityNotFoundException("Mercado não encontrado."));

        if(endDate == null){
            endDate = LocalDate.now();
        }

        if(startDate == null){
            startDate = endDate.minusDays(30);
        }

        if(startDate.isAfter(endDate)){
            throw new BusinessRuleException("Data final não pode ser antes da data inicial.");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        Page<BackupHistory> backupHistoryPage = backupHistoryGateway.findByPeriod(marketId, startDateTime, endDateTime, pageable);

        return new GetBackupResponse(backupHistoryPage, market.getName());
    }

    public void checkMissedBackups(){

        List<Market> activeMarkets = marketGateway.findAllActive();

        for (Market market : activeMarkets){

            LocalDateTime expectedDateTime = LocalDate.now().atTime(market.getExpectedBackupTime());
            LocalDateTime deadline = expectedDateTime.plus(BACKUP_TOLERANCE_TIME);

            if (LocalDateTime.now().isBefore(deadline)){
                continue;
            }

            Optional<BackupHistory> lastOptionalBackup = backupHistoryGateway.findLatestByMarketId(market.getId());

            if(lastOptionalBackup.isPresent()){
                BackupHistory lastBackup = lastOptionalBackup.get();

                if(lastBackup.getCreatedAt().toLocalDate().isEqual(LocalDate.now())){

                    if(lastBackup.getStatus() == BackupStatus.SUCCESS){
                        continue;
                    }

                    if(lastBackup.getStatus() == BackupStatus.PENDING && LocalDateTime.now().isBefore(lastBackup.getCreatedAt().plus(UPLOAD_URL_EXPIRATION))){
                        continue;
                    }

                    if(lastBackup.getStatus() == BackupStatus.FAILED){
                        continue;
                    }

                    lastBackup.setStatus(BackupStatus.FAILED);
                    lastBackup.setCompletedAt(LocalDateTime.now());
                    lastBackup.setErrorMessage("Tempo limite de upload excedido.");
                    backupHistoryGateway.save(lastBackup);
                    continue;
                }
            }

            BackupHistory failedBackupHistory = new BackupHistory(
                    UUID.randomUUID(),
                    market.getId(),
                    LocalDateTime.now(),
                    LocalDateTime.now(),
                    0L,
                    null,
                    "NONE",
                    BackupStatus.FAILED,
                    "Tempo limite expirado."
            );

            backupHistoryGateway.save(failedBackupHistory);
        }
    }
}
