package com.marketsense.stock.client;

import com.marketsense.common.dto.CompanyProfileDto;
import com.marketsense.common.dto.StockQuoteDto;
import com.marketsense.common.dto.StockSearchResponse;
import com.marketsense.insight.dto.CompanyNewsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "finnhubClient", url = "https://finnhub.io/api/v1")
public interface FinnhubClient {
    @GetMapping("/search")
    StockSearchResponse searchSymbol(
            @RequestParam("q") String query,
            @RequestParam("exchange") String exchange,
            @RequestParam("token") String apiKey
    );

    @GetMapping("/stock/profile2")
    CompanyProfileDto getProfile(
            @RequestParam("symbol") String symbol,
            @RequestParam("token") String apiKey
    );

    @GetMapping("/quote")
    StockQuoteDto getQuote(
            @RequestParam("symbol") String symbol,
            @RequestParam("token") String apiKey
    );

    @GetMapping("/company-news")
    List<CompanyNewsDto> getLatestNewsFromCompany(
            @RequestParam("symbol") String symbol,
            @RequestParam("from") String from,
            @RequestParam("to") String to,
            @RequestParam("token") String apiKey
    );

//    @GetMapping("/stock/insider-sentiment")
//    InsiderSentimentResponseDto getInsiderSentiment(
//            @RequestParam("symbol") String symbol,
//            @RequestParam("from") String fromDate, // Format: YYYY-MM-DD
//            @RequestParam("to") String toDate,     // Format: YYYY-MM-DD
//            @RequestParam("token") String apiKey
//    );
}
