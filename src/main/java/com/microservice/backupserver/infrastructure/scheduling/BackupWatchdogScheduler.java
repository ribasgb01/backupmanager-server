package com.microservice.backupserver.infrastructure.scheduling;

import com.microservice.backupserver.application.usecases.BackupHistoryUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class BackupWatchdogScheduler {

    private final BackupHistoryUseCase backupHistoryUseCase;

    @Scheduled(cron = "0 0 3-8 * * *")
    public void runMissingBackupCheck(){
        backupHistoryUseCase.checkMissedBackups();
    }
}