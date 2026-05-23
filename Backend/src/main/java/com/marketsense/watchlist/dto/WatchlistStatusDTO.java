package com.marketsense.watchlist.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WatchlistStatusDTO {
    private Long id;
    private String name;
    private String symbol;
    private boolean tracked;

    public WatchlistStatusDTO(Long id, String symbol, boolean tracked) {
        this.id = id;
        this.symbol = symbol;
        this.tracked = tracked;
    }
}
