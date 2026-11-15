package com.marketsense.digest;

import com.marketsense.insight.dto.MarketInsightDto;
import com.marketsense.user.User;
import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DigestMailerTest {

    private static final LocalDate DATE = LocalDate.of(2026, 11, 16);

    private JavaMailSender mailSender;
    private DigestMailer mailer;
    private final User user = DigestServiceTest.user(7L, "jane@example.com");

    private final StockDigestItem apple = new StockDigestItem("AAPL", "Apple Inc", 190.12, 1.2, 0.64,
            new MarketInsightDto("AAPL", "BULLISH", "Earnings beat expectations", 0.82, List.of()),
            List.of(new StockDigestItem.Headline("Apple beats Q4 estimates", "https://news.example/apple", "Reuters")));
    private final StockDigestItem tesla = new StockDigestItem("TSLA", "Tesla", null, null, null, null, List.of());

    @BeforeEach
    void setUp() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(inv -> new MimeMessage(Session.getInstance(new Properties())));

        DigestProperties properties = new DigestProperties(true, "0 0 8 * * MON-FRI", "Asia/Singapore",
                "MarketSense <digest@marketsense.test>", "http://api.test", "http://web.test", 0);
        mailer = new DigestMailer(mailSender, engine, new UnsubscribeTokenService("test-secret"), properties);
        user.setFirstName("Jane");
    }

    @Test
    void rendersEveryStockWithPriceSentimentAndHeadlines() {
        String html = mailer.render(user, List.of(apple, tesla), DATE, "http://api.test/unsub");

        assertThat(html)
                .contains("Good morning, Jane.")
                .contains("Monday, 16 November 2026")
                .contains("AAPL", "Apple Inc", "$190.12", "▲ +1.20 (+0.64%)")
                .contains("BULLISH", "Confidence 82%", "Earnings beat expectations")
                .contains("href=\"https://news.example/apple\"", "Apple beats Q4 estimates", "Reuters")
                .contains("href=\"http://web.test/stock/AAPL\"")
                .contains("TSLA", "Price unavailable", "AI insight unavailable today.")
                .contains("href=\"http://api.test/unsub\"", "href=\"http://web.test/profile\"")
                .contains("not financial advice");
    }

    @Test
    void escapesHtmlInThirdPartyText() {
        StockDigestItem sneaky = new StockDigestItem("EVIL", "<script>alert(1)</script>", 1.0, 0.1, 0.1, null,
                List.of(new StockDigestItem.Headline("<img src=x onerror=alert(1)>", null, null)));

        String html = mailer.render(user, List.of(sneaky), DATE, "http://api.test/unsub");

        assertThat(html).doesNotContain("<script>alert(1)</script>").doesNotContain("<img src=x");
        assertThat(html).contains("&lt;script&gt;");
    }

    @Test
    void sendsToUserWithSubjectAndUnsubscribeHeaders() throws Exception {
        mailer.send(user, List.of(apple), DATE);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender).send(captor.capture());
        MimeMessage message = captor.getValue();

        assertThat(message.getSubject()).isEqualTo("Your MarketSense digest — Mon, 16 Nov");
        assertThat(((InternetAddress) message.getAllRecipients()[0]).getAddress()).isEqualTo("jane@example.com");
        assertThat(((InternetAddress) message.getFrom()[0]).getAddress()).isEqualTo("digest@marketsense.test");

        String expectedUnsubscribe = "http://api.test/api/digest/unsubscribe?uid=7&sig="
                + new UnsubscribeTokenService("test-secret").sign(7L);
        assertThat(message.getHeader("List-Unsubscribe")[0]).isEqualTo("<" + expectedUnsubscribe + ">");
        assertThat(message.getHeader("List-Unsubscribe-Post")[0]).isEqualTo("List-Unsubscribe=One-Click");
    }

    @Test
    void plainTextVersionCoversTheEssentials() {
        String text = mailer.plainText(user, List.of(apple, tesla), DATE, "http://api.test/unsub");

        assertThat(text).contains("AAPL  $190.12  ▲ +1.20 (+0.64%)", "BULLISH (Confidence 82%)",
                "- Apple beats Q4 estimates", "TSLA  —  Price unavailable", "Unsubscribe: http://api.test/unsub");
    }

    @Test
    void formatsNegativeChangeInRed() {
        assertThat(DigestMailer.formatChange(-2.5, -1.25)).isEqualTo("▼ -2.50 (-1.25%)");
        assertThat(DigestMailer.changeColor(-2.5)).isEqualTo("#f87171");
        assertThat(DigestMailer.sentimentColor("NEUTRAL")).isEqualTo("#fbbf24");
    }
}
