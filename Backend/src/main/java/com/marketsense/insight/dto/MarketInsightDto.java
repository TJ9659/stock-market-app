package com.marketsense.insight.dto;

import java.util.List;

public record MarketInsightDto(
        String symbol,
        String sentiment,
        String catalyst,
        double confidence,
        List<String> keyPoints
) {}
