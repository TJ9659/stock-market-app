package com.marketsense.insight;

import com.marketsense.insight.dto.MarketInsightDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/insight")
@RequiredArgsConstructor
public class InsightController {

    private final InsightService insightService;

    @GetMapping("/{symbol}")
    public MarketInsightDto getStockInsight(@PathVariable String symbol) {
        return insightService.analyzeStock(symbol);
    }
}
