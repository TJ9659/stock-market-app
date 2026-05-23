package com.marketsense.watchlist.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class WatchlistRequest {
    @NotBlank(message = "Stock symbol is required")
    @Size(min = 1, max = 10, message = "Symbol must be between 1 and 10 characters")
    private String symbol;
    private String name;
}
