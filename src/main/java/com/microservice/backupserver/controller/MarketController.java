package com.microservice.backupserver.controller;

import com.microservice.backupserver.application.usecases.MarketUseCase;
import com.microservice.backupserver.controller.dto.request.CreateMarketRequest;
import com.microservice.backupserver.controller.dto.response.MarketResponse;
import com.microservice.backupserver.domain.Market;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/markets")
@RequiredArgsConstructor
public class MarketController {

    private final MarketUseCase marketUseCase;

    @PostMapping("/create")
    public ResponseEntity<MarketResponse> createMarket(@RequestBody @Valid CreateMarketRequest request){

        Market createdMarket = marketUseCase.registerMarket(
                request.name(),
                request.cnpj(),
                request.expectedBackupTime()
        );

        var response = new MarketResponse(
                createdMarket.getId(),
                createdMarket.getName(),
                createdMarket.getCnpj(),
                createdMarket.getApiKey(),
                createdMarket.getExpectedBackupTime(),
                createdMarket.getActive()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<MarketResponse>> listActiveMarkets(){

        var marketList = marketUseCase.listActiveMarkets();

        var response = marketList.stream()
                .map(market -> new MarketResponse(
                        market.getId(),
                        market.getName(),
                        market.getCnpj(),
                        market.getApiKey(),
                        market.getExpectedBackupTime(),
                        market.getActive()
                )).toList();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MarketResponse> findMarketById(@PathVariable UUID marketId){

        var market = marketUseCase.getMarketById(marketId);

        var response = new MarketResponse(
                market.getId(),
                market.getName(),
                market.getCnpj(),
                market.getApiKey(),
                market.getExpectedBackupTime(),
                market.getActive()
        );

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
