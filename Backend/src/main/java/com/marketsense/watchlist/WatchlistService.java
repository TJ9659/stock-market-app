package com.marketsense.watchlist;


import com.marketsense.common.dto.StockQuoteDto;
import com.marketsense.common.exceptions.ResourceNotFoundException;
import com.marketsense.stock.StockService;
import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import com.marketsense.watchlist.dto.WatchlistResponse;
import com.marketsense.watchlist.dto.WatchlistStatusDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WatchlistService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;

    private final StockService stockService;

    public WatchlistService(WatchlistRepository watchlistRepository, UserRepository userRepository, StockService stockService) {
        this.watchlistRepository = watchlistRepository;
        this.userRepository = userRepository;
        this.stockService = stockService;
    }

    public Page<WatchlistResponse> getWatchlist(String email, String keyword, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Watchlist> entities;

        if (keyword == null || keyword.trim().isEmpty()) {
            entities = watchlistRepository.findByUserId(user.getId(), pageable);
        }else{
            entities = watchlistRepository.searchUserWatchlist(user.getId(), keyword, pageable);
        }


        return entities.map(item -> {
            StockQuoteDto quote = stockService.getStockQuote(item.getSymbol());

            return WatchlistResponse.builder()
                    .id(item.getId())
                    .symbol(item.getSymbol())
                    .currentPrice(quote.getCurrentPrice())
                    .change(quote.getChange())
                    .percentChange(quote.getPercentChange())
                    .build();
        });
    }

    public WatchlistStatusDTO isSymbolInWatchlist(String email, String symbol) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Watchlist item = watchlistRepository.findByUserIdAndSymbol(user.getId(), symbol);

        if (item == null) {
            return new WatchlistStatusDTO(null,  symbol, false);
        }

        return new WatchlistStatusDTO(item.getId(), item.getSymbol(), true);
    }

    public WatchlistStatusDTO saveToWatchlist(String email, String name, String symbol) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Watchlist existingItem = watchlistRepository.findByUserIdAndSymbol(user.getId(), symbol);
        if (existingItem != null) {
            return new WatchlistStatusDTO(existingItem.getId(), existingItem.getName(), existingItem.getSymbol(), true);
        }

        Watchlist item = new Watchlist();
        item.setUser(user);
        item.setName(name);
        item.setSymbol(symbol);

        Watchlist savedItem = watchlistRepository.save(item);

        return new WatchlistStatusDTO(savedItem.getId(), savedItem.getSymbol(), true);
    }

    public void deleteFromWatchlist(Long id) {
        if (!watchlistRepository.existsById(id)) {
            throw new ResourceNotFoundException("Watchlist item not found");
        }
        watchlistRepository.deleteById(id);
    }
}
