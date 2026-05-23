package com.marketsense.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class StockQuoteDto {
    @JsonAlias("c")
    private Double currentPrice;

    @JsonAlias("d")
    private Double change;

    @JsonAlias("dp")
    private Double percentChange;

    @JsonAlias("h")
    private Double high;

    @JsonAlias("l")
    private Double low;

    @JsonAlias("o")
    private Double open;

    @JsonAlias("pc")
    private Double previousClose;

    @JsonAlias("t")
    private Long timestamp;
}