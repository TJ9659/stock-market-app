package com.marketsense.stock;

import com.marketsense.common.dto.CompanyProfileDto;
import com.marketsense.common.dto.StockQuoteDto;
import com.marketsense.common.dto.StockSearchResponse;
import com.marketsense.common.dto.StockSymbolDto;
import com.marketsense.insight.dto.CompanyNewsDto;
import com.marketsense.stock.client.FinnhubClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final FinnhubClient finnhubClient;

    @Value("${finnhub.api-key}")
    private String apiKey;

    public List<StockSymbolDto> searchStocks(String query) {
        StockSearchResponse response = finnhubClient.searchSymbol(query, "US", apiKey);
        return response.getResult();
    }


    @Cacheable(value = "stocks", key = "#symbol")
    public StockQuoteDto getStockQuote(String symbol){
        StockQuoteDto quote = finnhubClient.getQuote(symbol, apiKey);

        return quote;
    }

    public CompanyProfileDto getCompanyProfile(String symbol) {
        CompanyProfileDto profile = finnhubClient.getProfile(symbol, apiKey);

        if (profile == null || profile.getTicker() == null) {
            throw new RuntimeException("Stock profile not found for symbol: " + symbol);
        }

        return profile;
    }

    public List<CompanyNewsDto> getLatestNewsFromCompany(String symbol, String from, String to){
        List<CompanyNewsDto> news = finnhubClient.getLatestNewsFromCompany(symbol, from, to, apiKey);

        return news;
    }

}
