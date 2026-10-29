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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BackupHistoryUseCaseTest {

    @Mock
    private BackupHistoryGateway backupHistoryGateway;

    @Mock
    private FileStorageGateway fileStorageGateway;

    @Mock
    private MarketGateway marketGateway;

    @InjectMocks
    private BackupHistoryUseCase backupHistoryUseCase;

    private Market market(UUID id) {

        return Market.builder()
                .id(id)
                .name("Mercado")
                .expectedBackupTime(LocalTime.MIDNIGHT)
                .active(true)
                .build();
    }

    private BackupHistory backup(UUID id, UUID marketId, BackupStatus status) {
        return BackupHistory.builder()
                .id(id)
                .marketId(marketId)
                .createdAt(LocalDateTime.now())
                .s3Key("key")
                .status(status)
                .build();
    }

    @Test
    @DisplayName("Deve solicitar upload e persistir backup pendente")
    void requestBackupUploadCase1() {

        UUID marketId = UUID.randomUUID();
        Market market = market(marketId);

        when(fileStorageGateway.generateUploadPresignedUrl(anyString(), any(Duration.class)))
                .thenReturn("upload-url");

        when(backupHistoryGateway.save(any(BackupHistory.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        BackupUploadResponse response = backupHistoryUseCase
                .requestBackupUpload(market, "backup.fbk", 123L);

        assertNotNull(response.backupId());
        assertEquals("upload-url", response.uploadUrl());

        ArgumentCaptor<BackupHistory> captor = ArgumentCaptor.forClass(BackupHistory.class);

        verify(backupHistoryGateway).save(captor.capture());
        BackupHistory saved = captor.getValue();

        assertEquals(marketId, saved.getMarketId());
        assertEquals(BackupStatus.PENDING, saved.getStatus());
        assertEquals(123L, saved.getFileSizeBytes());
        assertTrue(saved.getS3Key().contains("backup.fbk"));

        verify(fileStorageGateway).generateUploadPresignedUrl(contains(marketId.toString()), eq(Duration.ofMinutes(30)));
    }

    @Test
    @DisplayName("Deve lançar exceção ao concluir backup inexistente")
    void completeBackupCase1() {

        UUID id = UUID.randomUUID();

        when(backupHistoryGateway.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> backupHistoryUseCase.completeBackup(market
                                (UUID.randomUUID()),
                        id,
                        BackupStatus.SUCCESS,
                        null,
                        "hash"));

        verify(backupHistoryGateway, never()).save(any());
    }

    @Test
    @DisplayName("Deve impedir mercado de concluir backup de outro mercado")
    void completeBackupCase2() {

        UUID id = UUID.randomUUID();

        BackupHistory history = backup(id, UUID.randomUUID(), BackupStatus.PENDING);

        when(backupHistoryGateway.findById(id))
                .thenReturn(Optional.of(history));

        assertThrows(BusinessRuleException.class,
                () -> backupHistoryUseCase.completeBackup(market(
                                UUID.randomUUID()),
                        id,
                        BackupStatus.SUCCESS,
                        null,
                        "hash"));

        verify(backupHistoryGateway, never()).save(any());
    }

    @Test
    @DisplayName("Deve impedir concluir backup que não está pendente")
    void completeBackupCase3() {

        UUID marketId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        BackupHistory history = backup(id, marketId, BackupStatus.SUCCESS);

        when(backupHistoryGateway.findById(id))
                .thenReturn(Optional.of(history));

        assertThrows(BusinessRuleException.class,
                () -> backupHistoryUseCase.completeBackup(
                        market(marketId),
                        id,
                        BackupStatus.SUCCESS,
                        null,
                        "hash")
        );

        verify(backupHistoryGateway, never()).save(any());
    }

    @Test
    @DisplayName("Deve exigir mensagem de erro para backup FAILED")
    void completeBackupCase4() {

        UUID marketId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        BackupHistory history = backup(id, marketId, BackupStatus.PENDING);

        when(backupHistoryGateway.findById(id))
                .thenReturn(Optional.of(history));

        assertThrows(IllegalArgumentException.class,
                () -> backupHistoryUseCase.completeBackup(
                        market(marketId),
                        id,
                        BackupStatus.FAILED,
                        null,
                        null)
        );

        verify(backupHistoryGateway, never()).save(any());
    }

    @Test
    @DisplayName("Deve concluir backup com os dados informados")
    void completeBackupCase5() {

        UUID marketId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        BackupHistory history = backup(
                id,
                marketId,
                BackupStatus.PENDING
        );

        when(backupHistoryGateway.findById(id))
                .thenReturn(Optional.of(history));

        when(backupHistoryGateway.save(any()))
                .thenAnswer(inv -> inv.getArgument(0));

        backupHistoryUseCase.completeBackup(
                market(marketId),
                id,
                BackupStatus.SUCCESS,
                null,
                "md5"
        );

        assertEquals(BackupStatus.SUCCESS, history.getStatus());
        assertEquals("md5", history.getFileHashMd5());
        assertNotNull(history.getCompletedAt());

        verify(backupHistoryGateway).save(history);
    }

    @Test
    @DisplayName("Deve lançar exceção ao solicitar download de backup inexistente")
    void requestBackupDownloadCase1() {

        UUID id = UUID.randomUUID();

        when(backupHistoryGateway.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> backupHistoryUseCase.requestBackupDownload(id));

        verifyNoInteractions(fileStorageGateway);
    }

    @Test
    @DisplayName("Deve impedir download de backup que não teve sucesso")
    void requestBackupDownloadCase2() {

        UUID id = UUID.randomUUID();

        when(backupHistoryGateway.findById(id))
                .thenReturn(Optional.of(backup(
                        id,
                        UUID.randomUUID(),
                        BackupStatus.FAILED
                )));

        assertThrows(BusinessRuleException.class,
                () -> backupHistoryUseCase.requestBackupDownload(id));

        verifyNoInteractions(fileStorageGateway);
    }

    @Test
    @DisplayName("Deve gerar URL para download de backup concluído com sucesso")
    void requestBackupDownloadCase3() {

        UUID id = UUID.randomUUID();

        when(backupHistoryGateway.findById(id))
                .thenReturn(Optional.of(backup(
                        id,
                        UUID.randomUUID(),
                        BackupStatus.SUCCESS
                )));

        when(fileStorageGateway.generateDownloadPresignedUrl("key", Duration.ofMinutes(15)))
                .thenReturn("download-url");

        assertEquals("download-url", backupHistoryUseCase.requestBackupDownload(id));

        verify(fileStorageGateway).generateDownloadPresignedUrl("key", Duration.ofMinutes(15));
    }

    @Test
    @DisplayName("Deve lançar exceção quando mercado do histórico não existir")
    void getBackupHistoryCase1() {

        UUID id = UUID.randomUUID();

        when(marketGateway.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> backupHistoryUseCase.getBackupHistory(
                        id,
                        null,
                        null,
                        PageRequest.of(0, 10
                        )));
    }

    @Test
    @DisplayName("Deve rejeitar período com data inicial posterior à final")
    void getBackupHistoryCase2() {
        UUID id = UUID.randomUUID();
        when(marketGateway.findById(id))
                .thenReturn(Optional.of(market(id)));

        assertThrows(BusinessRuleException.class,
                () -> backupHistoryUseCase.getBackupHistory(
                        id,
                        LocalDate.of(2026, 2, 1),
                        LocalDate.of(2026, 1, 1),
                        PageRequest.of(0, 10)));

        verify(backupHistoryGateway, never()).findByPeriod(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve consultar histórico no período informado")
    void getBackupHistoryCase3() {

        UUID id = UUID.randomUUID();
        Market market = market(id);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<BackupHistory> page = new PageImpl<>(List.of(backup(UUID.randomUUID(), id, BackupStatus.SUCCESS)));

        LocalDate start = LocalDate.of(2026, 1, 1);
        LocalDate end = LocalDate.of(2026, 1, 31);

        when(marketGateway.findById(id))
                .thenReturn(Optional.of(market));

        when(backupHistoryGateway.findByPeriod(eq(id), any(LocalDateTime.class), any(LocalDateTime.class), eq(pageable)))
                .thenReturn(page);

        GetBackupResponse response = backupHistoryUseCase.getBackupHistory(id, start, end, pageable);

        assertSame(page, response.backupHistoryPage());
        assertEquals("Mercado", response.marketName());

        verify(backupHistoryGateway).findByPeriod(eq(id), eq(start.atStartOfDay()), eq(end.atTime(LocalTime.MAX)), eq(pageable));
    }

    @Test
    @DisplayName("Deve usar período padrão quando datas não forem informadas")
    void getBackupHistoryCase4() {

        UUID id = UUID.randomUUID();

        when(marketGateway.findById(id))
                .thenReturn(Optional.of(market(id)));

        when(backupHistoryGateway.findByPeriod(eq(id), any(), any(), any()))
                .thenReturn(Page.empty());

        backupHistoryUseCase.getBackupHistory(id, null, null, PageRequest.of(0, 10));

        ArgumentCaptor<LocalDateTime> start = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> end = ArgumentCaptor.forClass(LocalDateTime.class);

        verify(backupHistoryGateway).findByPeriod(eq(id), start.capture(), end.capture(), any());

        assertEquals(LocalDate.now().minusDays(30), start.getValue().toLocalDate());
        assertEquals(LocalDate.now(), end.getValue().toLocalDate());
        assertEquals(LocalTime.MAX, end.getValue().toLocalTime());
    }

    @Test
    @DisplayName("Não deve gerar falha para mercado sem backups antes do horário limite")
    void checkMissedBackupsCase1() {

        UUID id = UUID.randomUUID();
        Market market = market(id);
        market.setExpectedBackupTime(LocalTime.MAX);

        when(marketGateway.findAllActive())
                .thenReturn(List.of(market));

        backupHistoryUseCase.checkMissedBackups();

        verify(backupHistoryGateway, never()).findLatestByMarketId(any());
        verify(backupHistoryGateway, never()).save(any());
    }
}
