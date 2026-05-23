package com.marketsense.stock;

import com.marketsense.common.dto.CompanyProfileDto;
import com.marketsense.common.dto.StockQuoteDto;
import com.marketsense.common.dto.StockSymbolDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<StockSymbolDto>> search(@RequestParam String query) {
        return ResponseEntity.ok(stockService.searchStocks(query));
    }

    @GetMapping("/profile")
    public ResponseEntity<CompanyProfileDto> getCompanyProfile(@RequestParam("symbol") String symbol) {
        return ResponseEntity.ok(stockService.getCompanyProfile(symbol));
    }

    @GetMapping("/quote")
    public ResponseEntity<StockQuoteDto> getStockQuote(@RequestParam("symbol") String symbol){
        return ResponseEntity.ok(stockService.getStockQuote(symbol));
    }


}




