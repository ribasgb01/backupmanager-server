package com.microservice.backupserver.application.gateways;

import com.microservice.backupserver.domain.Market;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MarketGateway {

    Market save (Market market);
    Optional<Market> findById(UUID id);
    List<Market> findAllActive();
    Optional<Market> findByApiKey(String apiKey);
    boolean existsByCnpj(String cnpj);
}
