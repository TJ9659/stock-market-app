package com.marketsense.digest;

import com.marketsense.common.dto.StockQuoteDto;
import com.marketsense.common.exceptions.AiAnalysisException;
import com.marketsense.common.exceptions.EmptyWatchlistException;
import com.marketsense.insight.InsightService;
import com.marketsense.insight.dto.CompanyNewsDto;
import com.marketsense.insight.dto.MarketInsightDto;
import com.marketsense.stock.StockService;
import com.marketsense.user.User;
import com.marketsense.watchlist.Watchlist;
import com.marketsense.watchlist.WatchlistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DigestServiceTest {

    private WatchlistRepository watchlistRepository;
    private StockService stockService;
    private InsightService insightService;
    private DigestMailer digestMailer;
    private DigestService digestService;

    private final User alice = user(1L, "alice@example.com");
    private final User bob = user(2L, "bob@example.com");

    @BeforeEach
    void setUp() {
        watchlistRepository = mock(WatchlistRepository.class);
        stockService = mock(StockService.class);
        insightService = mock(InsightService.class);
        digestMailer = mock(DigestMailer.class);
        DigestProperties properties = new DigestProperties(true, "0 0 8 * * MON-FRI", "Asia/Singapore",
                "MarketSense <digest@test>", "http://api", "http://web", 0);
        digestService = new DigestService(watchlistRepository, stockService, insightService, digestMailer, properties);

        when(stockService.getStockQuote(anyString())).thenAnswer(inv -> quote(100.0, 1.5, 1.52));
        when(stockService.getLatestNewsFromCompany(anyString(), anyString(), anyString()))
                .thenAnswer(inv -> List.of(news("Headline A", "https://news/a"), news("Headline B", "https://news/b"),
                        news("Headline C", "https://news/c")));
        when(insightService.getAnalysis(anyString(), anyList()))
                .thenAnswer(inv -> new MarketInsightDto(inv.getArgument(0), "BULLISH", "Strong quarter", 0.8, List.of()));
    }

    @Test
    void sendsOneEmailPerUserWithTheirOwnStocks() {
        when(watchlistRepository.findAllForDigest()).thenReturn(List.of(
                row(alice, "AAPL", "Apple"), row(alice, "MSFT", "Microsoft"), row(bob, "TSLA", "Tesla")));

        DigestRunResult result = digestService.sendToAll();

        assertThat(result).isEqualTo(new DigestRunResult(2, 0, 3));
        assertThat(symbolsSentTo(alice)).containsExactly("AAPL", "MSFT");
        assertThat(symbolsSentTo(bob)).containsExactly("TSLA");
    }

    @Test
    void fetchesEachSymbolOnlyOnceAcrossUsers() {
        when(watchlistRepository.findAllForDigest()).thenReturn(List.of(
                row(alice, "AAPL", "Apple"), row(bob, "aapl", "Apple"), row(bob, "NVDA", "Nvidia")));

        DigestRunResult result = digestService.sendToAll();

        assertThat(result.symbolsProcessed()).isEqualTo(2);
        verify(stockService, times(1)).getStockQuote("AAPL");
        verify(stockService, times(1)).getLatestNewsFromCompany(eq("AAPL"), anyString(), anyString());
        verify(insightService, times(1)).getAnalysis(eq("AAPL"), anyList());
    }

    @Test
    void buildsItemWithQuoteTopTwoHeadlinesAndInsight() {
        StockDigestItem item = digestService.buildItem("AAPL", "Apple");

        assertThat(item.price()).isEqualTo(100.0);
        assertThat(item.percentChange()).isEqualTo(1.52);
        assertThat(item.insight().sentiment()).isEqualTo("BULLISH");
        assertThat(item.headlines()).extracting(StockDigestItem.Headline::title).containsExactly("Headline A", "Headline B");
        verify(insightService).getAnalysis("AAPL", List.of("Headline A", "Headline B", "Headline C"));
    }

    @Test
    void sendsAtMostTenHeadlinesToTheAi() {
        when(stockService.getLatestNewsFromCompany(anyString(), anyString(), anyString())).thenReturn(
                IntStream.rangeClosed(1, 15).mapToObj(i -> news("H" + i, null)).toList());

        digestService.buildItem("AAPL", "Apple");

        ArgumentCaptor<List<String>> headlines = ArgumentCaptor.forClass(List.class);
        verify(insightService).getAnalysis(eq("AAPL"), headlines.capture());
        assertThat(headlines.getValue()).hasSize(10).first().isEqualTo("H1");
    }

    @Test
    void aiFailureStillProducesItemWithoutInsight() {
        when(insightService.getAnalysis(anyString(), anyList())).thenThrow(new AiAnalysisException("boom"));

        StockDigestItem item = digestService.buildItem("AAPL", "Apple");

        assertThat(item.insight()).isNull();
        assertThat(item.price()).isEqualTo(100.0);
        assertThat(item.headlines()).hasSize(2);
    }

    @Test
    void zeroQuoteFromFinnhubIsTreatedAsUnavailable() {
        when(stockService.getStockQuote("XXXX")).thenReturn(quote(0.0, 0.0, 0.0));

        StockDigestItem item = digestService.buildItem("XXXX", "Unknown");

        assertThat(item.price()).isNull();
        assertThat(item.change()).isNull();
    }

    @Test
    void dropsNonHttpHeadlineLinks() {
        when(stockService.getLatestNewsFromCompany(anyString(), anyString(), anyString()))
                .thenReturn(List.of(news("Bad link", "javascript:alert(1)")));

        StockDigestItem item = digestService.buildItem("AAPL", "Apple");

        assertThat(item.headlines().get(0).url()).isNull();
    }

    @Test
    void oneFailedEmailDoesNotStopTheOthers() {
        when(watchlistRepository.findAllForDigest()).thenReturn(List.of(row(alice, "AAPL", "Apple"), row(bob, "AAPL", "Apple")));
        doThrow(new MailSendException("SMTP down")).when(digestMailer).send(eq(alice), anyList(), any());

        DigestRunResult result = digestService.sendToAll();

        assertThat(result.usersSent()).isEqualTo(1);
        assertThat(result.usersFailed()).isEqualTo(1);
        verify(digestMailer).send(eq(bob), anyList(), any());
    }

    @Test
    void noSubscribersMeansNothingIsFetchedOrSent() {
        when(watchlistRepository.findAllForDigest()).thenReturn(List.of());

        assertThat(digestService.sendToAll()).isEqualTo(new DigestRunResult(0, 0, 0));
        verify(stockService, never()).getStockQuote(anyString());
        verify(digestMailer, never()).send(any(), anyList(), any());
    }

    @Test
    void testSendToOneUserReturnsStockCount() {
        when(watchlistRepository.findAllByUserIdWithUser(1L)).thenReturn(List.of(row(alice, "AAPL", "Apple"), row(alice, "MSFT", "Microsoft")));

        assertThat(digestService.sendToUser(1L)).isEqualTo(2);
        verify(digestMailer).send(eq(alice), anyList(), any());
    }

    @Test
    void testSendWithEmptyWatchlistIsRejected() {
        when(watchlistRepository.findAllByUserIdWithUser(1L)).thenReturn(List.of());

        assertThatThrownBy(() -> digestService.sendToUser(1L)).isInstanceOf(EmptyWatchlistException.class);
        verify(digestMailer, never()).send(any(), anyList(), any());
    }

    @SuppressWarnings("unchecked")
    private List<String> symbolsSentTo(User user) {
        ArgumentCaptor<List<StockDigestItem>> items = ArgumentCaptor.forClass(List.class);
        verify(digestMailer).send(eq(user), items.capture(), any());
        return items.getValue().stream().map(StockDigestItem::symbol).toList();
    }

    static User user(Long id, String email) {
        User user = new User("Test", "User", email, "hashed");
        user.setId(id);
        return user;
    }

    private static Watchlist row(User user, String symbol, String name) {
        Watchlist row = new Watchlist();
        row.setUser(user);
        row.setSymbol(symbol);
        row.setName(name);
        return row;
    }

    private static StockQuoteDto quote(double price, double change, double percent) {
        StockQuoteDto quote = new StockQuoteDto();
        quote.setCurrentPrice(price);
        quote.setChange(change);
        quote.setPercentChange(percent);
        return quote;
    }

    private static CompanyNewsDto news(String headline, String url) {
        CompanyNewsDto news = new CompanyNewsDto();
        news.setHeadline(headline);
        news.setUrl(url);
        news.setSource("Reuters");
        return news;
    }
}
