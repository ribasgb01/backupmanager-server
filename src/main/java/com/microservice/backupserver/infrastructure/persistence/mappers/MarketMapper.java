package com.microservice.backupserver.infrastructure.persistence.mappers;

import com.microservice.backupserver.domain.Market;
import com.microservice.backupserver.infrastructure.persistence.entities.MarketEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MarketMapper {

    MarketEntity toEntity(Market domain);
    Market toDomain(MarketEntity entity);
}
