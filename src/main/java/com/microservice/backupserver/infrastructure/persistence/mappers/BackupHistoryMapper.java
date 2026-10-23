package com.microservice.backupserver.infrastructure.persistence.mappers;

import com.microservice.backupserver.domain.BackupHistory;
import com.microservice.backupserver.infrastructure.persistence.entities.BackupHistoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BackupHistoryMapper {

    @Mapping(source = "marketId", target = "market.id")
    BackupHistoryEntity toEntity(BackupHistory domain);

    @Mapping(source = "market.id", target = "marketId")
    BackupHistory toDomain(BackupHistoryEntity entity);
}
