package com.marketsense.controller;

import com.marketsense.dto.StockSymbolDto;
import com.marketsense.services.StockService;
import org.springframework.cloud.openfeign.FeignClient;
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
    }




