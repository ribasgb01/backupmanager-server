package com.microservice.backupserver.infrastructure.persistence;

import com.microservice.backupserver.infrastructure.persistence.entities.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findByUsername(String username);

    boolean existsByUsername(String username);

    @Modifying
    @Query("DELETE FROM UserEntity u WHERE u.id = :id")
    int deleteByIdReturningCount(UUID id);

    List<UserEntity> findByActiveTrue();
}
