package com.marketsense.digest;

import com.marketsense.common.dto.StockQuoteDto;
import com.marketsense.common.exceptions.EmptyWatchlistException;
import com.marketsense.insight.InsightService;
import com.marketsense.insight.dto.CompanyNewsDto;
import com.marketsense.insight.dto.MarketInsightDto;
import com.marketsense.stock.StockService;
import com.marketsense.user.User;
import com.marketsense.watchlist.Watchlist;
import com.marketsense.watchlist.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class DigestService {

    static final int HEADLINES_IN_EMAIL = 2;
    static final int HEADLINES_FOR_AI = 10;

    private final WatchlistRepository watchlistRepository;
    private final StockService stockService;
    private final InsightService insightService;
    private final DigestMailer digestMailer;
    private final DigestProperties properties;

    public DigestRunResult sendToAll() {
        List<Watchlist> rows = watchlistRepository.findAllForDigest();
        Map<String, StockDigestItem> items = buildItems(rows, properties.symbolDelayMs());

        Map<Long, List<Watchlist>> rowsByUser = new LinkedHashMap<>();
        for (Watchlist row : rows) {
            rowsByUser.computeIfAbsent(row.getUser().getId(), id -> new ArrayList<>()).add(row);
        }

        int sent = 0;
        int failed = 0;
        for (List<Watchlist> userRows : rowsByUser.values()) {
            User user = userRows.get(0).getUser();
            try {
                digestMailer.send(user, itemsFor(userRows, items), today());
                sent++;
            } catch (Exception e) {
                failed++;
                log.warn("Digest failed for user {}: {}", user.getId(), e.getMessage());
            }
        }

        log.info("Digest done: {} sent, {} failed, {} symbols", sent, failed, items.size());
        return new DigestRunResult(sent, failed, items.size());
    }

    public int sendToUser(Long userId) {
        List<Watchlist> rows = watchlistRepository.findAllByUserIdWithUser(userId);
        if (rows.isEmpty()) {
            throw new EmptyWatchlistException("Add at least one stock to your watchlist to receive a digest.");
        }
        List<StockDigestItem> items = itemsFor(rows, buildItems(rows, 0));
        digestMailer.send(rows.get(0).getUser(), items, today());
        return items.size();
    }

    StockDigestItem buildItem(String symbol, String name) {
        StockQuoteDto quote = null;
        try {
            quote = stockService.getStockQuote(symbol);
        } catch (Exception e) {
            log.warn("No quote for {}: {}", symbol, e.getMessage());
        }
        boolean hasQuote = quote != null && quote.getCurrentPrice() != null && quote.getCurrentPrice() > 0;

        List<CompanyNewsDto> news = List.of();
        try {
            LocalDate to = today();
            List<CompanyNewsDto> fetched = stockService.getLatestNewsFromCompany(symbol, to.minusDays(7).toString(), to.toString());
            if (fetched != null) {
                news = fetched.stream().filter(n -> n.getHeadline() != null && !n.getHeadline().isBlank()).toList();
            }
        } catch (Exception e) {
            log.warn("No news for {}: {}", symbol, e.getMessage());
        }

        MarketInsightDto insight = null;
        try {
            insight = insightService.getAnalysis(symbol, news.stream().map(CompanyNewsDto::getHeadline).limit(HEADLINES_FOR_AI).toList());
        } catch (Exception e) {
            log.warn("No AI insight for {}: {}", symbol, e.getMessage());
        }

        List<StockDigestItem.Headline> headlines = news.stream()
                .limit(HEADLINES_IN_EMAIL)
                .map(n -> new StockDigestItem.Headline(n.getHeadline(), safeUrl(n.getUrl()), n.getSource()))
                .toList();

        return new StockDigestItem(symbol, name,
                hasQuote ? quote.getCurrentPrice() : null,
                hasQuote ? quote.getChange() : null,
                hasQuote ? quote.getPercentChange() : null,
                insight, headlines);
    }

    private Map<String, StockDigestItem> buildItems(List<Watchlist> rows, long delayMs) {
        Map<String, String> names = new LinkedHashMap<>();
        rows.forEach(row -> names.putIfAbsent(normalize(row.getSymbol()), row.getName()));

        Map<String, StockDigestItem> items = new LinkedHashMap<>();
        names.forEach((symbol, name) -> {
            if (!items.isEmpty()) pause(delayMs);
            items.put(symbol, buildItem(symbol, name));
        });
        return items;
    }

    private static List<StockDigestItem> itemsFor(List<Watchlist> rows, Map<String, StockDigestItem> items) {
        return rows.stream().map(row -> items.get(normalize(row.getSymbol()))).filter(Objects::nonNull).distinct().toList();
    }

    private LocalDate today() {
        return LocalDate.now(ZoneId.of(properties.zone()));
    }

    private static String safeUrl(String url) {
        if (url == null) return null;
        String lower = url.trim().toLowerCase(Locale.ROOT);
        return lower.startsWith("https://") || lower.startsWith("http://") ? url.trim() : null;
    }

    private static String normalize(String symbol) {
        return symbol == null ? "" : symbol.trim().toUpperCase(Locale.ROOT);
    }

    private static void pause(long ms) {
        if (ms <= 0) return;
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
