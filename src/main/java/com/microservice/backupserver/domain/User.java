package com.microservice.backupserver.domain;

import com.microservice.backupserver.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID id;
    private String username;
    private String passwordHash;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
