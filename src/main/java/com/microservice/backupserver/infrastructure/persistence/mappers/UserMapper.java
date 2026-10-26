package com.microservice.backupserver.infrastructure.persistence.mappers;

import com.microservice.backupserver.domain.User;
import com.microservice.backupserver.infrastructure.persistence.entities.UserEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserEntity toEntity(User user);
    User toDomain(UserEntity entity);
}
