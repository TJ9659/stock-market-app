package com.marketsense.digest;

import com.marketsense.insight.dto.MarketInsightDto;

import java.util.List;

public record StockDigestItem(
        String symbol,
        String name,
        Double price,
        Double change,
        Double percentChange,
        MarketInsightDto insight,
        List<Headline> headlines
) {
    public record Headline(String title, String url, String source) {}
}
