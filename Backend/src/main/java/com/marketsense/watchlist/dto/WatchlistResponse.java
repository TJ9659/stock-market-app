package com.marketsense.watchlist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatchlistResponse {
    private Long id;
    private String symbol;
    private Double currentPrice;
    private Double change;
    private Double percentChange;
}
