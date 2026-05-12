package com.marketsense.services;

import com.marketsense.dto.StockSearchResponse;
import com.marketsense.dto.StockSymbolDto;
import com.marketsense.interfaces.FinnhubClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final FinnhubClient finnhubClient;

    @Value("${finnhub.api-key}")
    private String apiKey;

    public List<StockSymbolDto> searchStocks(String query) {
        StockSearchResponse response = finnhubClient.searchSymbol(query, apiKey);
        return response.getResult();
    }

}
