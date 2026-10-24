package com.microservice.backupserver.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Market {
    private UUID id;
    private String name;
    private String cnpj;
    private String apiKey;
    private LocalTime expectedBackupTime;
    private Boolean active;
}
