package com.marketsense.watchlist;


import com.marketsense.watchlist.dto.WatchlistRequest;
import com.marketsense.watchlist.dto.WatchlistResponse;
import com.marketsense.watchlist.dto.WatchlistStatusDTO;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping("/check")
    public ResponseEntity<WatchlistStatusDTO> checkIfTracked(@RequestParam String symbol, Authentication authentication) {
        WatchlistStatusDTO response = watchlistService.isSymbolInWatchlist(authentication.getName(), symbol);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/add")
    public ResponseEntity<WatchlistStatusDTO> addToWatchlist(@RequestBody WatchlistRequest request, Authentication authentication) {
        WatchlistStatusDTO response = watchlistService.saveToWatchlist(authentication.getName(), request.getName(), request.getSymbol());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFromWatchlist(@PathVariable Long id) {
        watchlistService.deleteFromWatchlist(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<WatchlistResponse>> getWatchlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(name="search", required = false) String search,
            Authentication authentication) {

        Page<WatchlistResponse> watchlistPage = watchlistService.getWatchlist(
                authentication.getName(), search, page, size);

        return ResponseEntity.ok(watchlistPage);
    }
}
