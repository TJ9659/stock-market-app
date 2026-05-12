package com.marketsense.dto;

import lombok.Data;

@Data
public class StockSymbolDto {
    private String description;
    private String displaySymbol;
    private String symbol;
    private String type;
}
