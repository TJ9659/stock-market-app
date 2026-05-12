package com.marketsense.dto;

import lombok.Data;

import java.util.List;

@Data
public class StockSearchResponse {
    private int count;
    private List<StockSymbolDto> result;
}


