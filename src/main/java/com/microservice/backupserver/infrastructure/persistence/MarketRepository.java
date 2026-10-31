package com.microservice.backupserver.infrastructure.persistence;

import com.microservice.backupserver.application.usecases.dto.MarketOverviewDTO;
import com.microservice.backupserver.infrastructure.persistence.entities.MarketEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketRepository extends JpaRepository<MarketEntity, UUID> {

    Optional<MarketEntity> findByApiKey(String apiKey);
    boolean existsByCnpj(String cnpj);
    List<MarketEntity> findByActiveTrue();

    @Query(value = """
    SELECT
        m.id,
        m.name,
        m.cnpj,
        m.expected_backup_time,
        b.id,
        b.created_at,
        b.completed_at,
        b.file_size_bytes,
        b.status
    FROM markets m
    LEFT JOIN LATERAL (
        SELECT *
        FROM backup_histories bh
        WHERE bh.market_id = m.id
        ORDER BY bh.created_at DESC
        LIMIT 1
    ) b ON TRUE
    WHERE m.active = TRUE
    """, nativeQuery = true)
    List<Object[]> findAllActiveWithOverview();

    @Query("SELECT COUNT(m) FROM MarketEntity m WHERE m.active=true")
    long countActiveMarkets();
}
