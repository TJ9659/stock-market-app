package com.marketsense.interfaces;

import com.marketsense.dto.StockSearchResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "finnhubClient", url = "https://finnhub.io/api/v1")
public interface FinnhubClient {
    @GetMapping("/search")
    StockSearchResponse searchSymbol(
            @RequestParam("q") String query,
            @RequestParam("token") String apiKey
    );
}
