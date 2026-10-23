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
    UUID id;
    String name;
    String cnpj;
    String apiKey;
    LocalTime expectedBackupTime;
    Boolean active;
}
