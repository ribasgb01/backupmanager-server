package com.microservice.backupserver.infrastructure.persistence;

import com.microservice.backupserver.infrastructure.persistence.entities.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {

    Optional<MarketEntity> findByApiKey(String apiKey);
    boolean existsByCnpj(String cnpj);
    List<MarketEntity> findByActiveTrue();
}
