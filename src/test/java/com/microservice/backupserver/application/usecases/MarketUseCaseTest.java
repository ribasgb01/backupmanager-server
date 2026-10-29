package com.microservice.backupserver.application.usecases;

import com.microservice.backupserver.application.exceptions.EntityAlreadyExistsException;
import com.microservice.backupserver.application.exceptions.EntityNotFoundException;
import com.microservice.backupserver.application.gateways.MarketGateway;
import com.microservice.backupserver.domain.Market;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MarketUseCaseTest {
    @Mock
    private MarketGateway marketGateway;
    @InjectMocks
    private MarketUseCase marketUseCase;

    @Test
    @DisplayName("Deve lançar exceção ao cadastrar CNPJ já existente")
    void registerMarketCase1() {

        when(marketGateway.existsByCnpj("cnpj"))
                .thenReturn(true);

        assertThrows(EntityAlreadyExistsException.class,
                () -> marketUseCase.registerMarket("Mercado", "cnpj", LocalTime.NOON));

        verify(marketGateway).existsByCnpj("cnpj");

        verify(marketGateway, never()).save(any());
    }

    @Test
    @DisplayName("Deve cadastrar mercado com API key e status ativo")
    void registerMarketCase2() {

        when(marketGateway.existsByCnpj("cnpj"))
                .thenReturn(false);

        when(marketGateway.save(any(Market.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        Market market = marketUseCase.registerMarket("Mercado", "cnpj", LocalTime.NOON);

        assertNotNull(market.getId());
        assertNotNull(market.getApiKey());
        assertEquals("Mercado", market.getName());
        assertEquals("cnpj", market.getCnpj());
        assertEquals(LocalTime.NOON, market.getExpectedBackupTime());
        assertTrue(market.getActive());

        verify(marketGateway).save(market);
    }

    @Test
    @DisplayName("Deve listar mercados ativos")
    void listActiveMarketsCase1() {

        List<Market> markets = List.of(Market.builder().
                id(UUID.randomUUID())
                .name("Mercado")
                .active(true)
                .build());

        when(marketGateway.findAllActive())
                .thenReturn(markets);

        assertSame(markets, marketUseCase.listActiveMarkets());

        verify(marketGateway).findAllActive();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não houver mercados ativos")
    void listActiveMarketsCase2() {

        when(marketGateway.findAllActive())
                .thenReturn(List.of());

        assertTrue(marketUseCase.listActiveMarkets().isEmpty());
    }

    @Test
    @DisplayName("Deve buscar mercado pelo ID")
    void getMarketByIdCase1() {

        UUID id = UUID.randomUUID();

        Market market = Market.builder()
                .id(id)
                .name("Mercado")
                .build();

        when(marketGateway.findById(id))
                .thenReturn(Optional.of(market));

        assertSame(market, marketUseCase.getMarketById(id));

        verify(marketGateway).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção quando mercado não for encontrado")
    void getMarketByIdCase2() {

        UUID id = UUID.randomUUID();

        when(marketGateway.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> marketUseCase.getMarketById(id));
    }
}
