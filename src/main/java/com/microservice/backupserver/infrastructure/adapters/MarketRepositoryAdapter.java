package com.microservice.backupserver.infrastructure.adapters;

import com.microservice.backupserver.application.gateways.MarketGateway;
import com.microservice.backupserver.domain.Market;
import com.microservice.backupserver.infrastructure.persistence.MarketRepository;
import com.microservice.backupserver.infrastructure.persistence.mappers.MarketMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MarketRepositoryAdapter implements MarketGateway {

    private final MarketRepository marketRepository;
    private final MarketMapper marketMapper;

    @Override
    public Market save(Market market) {
        return marketMapper.toDomain(
                marketRepository.save(
                        marketMapper.toEntity(market)
                )
        );
    }

    @Override
    public Optional<Market> findById(UUID id) {
        return marketRepository.findById(id)
                .map(marketMapper::toDomain);
    }

    @Override
    public List<Market> findAllActive() {
        return marketRepository.findByActiveTrue()
                .stream()
                .map(marketMapper::toDomain)
                .toList();
    }

    @Override
    public Optional<Market> findByApiKey(String apiKey) {
        return marketRepository.findByApiKey(apiKey)
                .map(marketMapper::toDomain);
    }

    @Override
    public boolean existsByCnpj(String cnpj) {
        return marketRepository.existsByCnpj(cnpj);
    }
}
