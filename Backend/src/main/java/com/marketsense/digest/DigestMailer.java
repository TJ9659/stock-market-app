package com.marketsense.digest;

import com.marketsense.insight.dto.MarketInsightDto;
import com.marketsense.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class DigestMailer {

    private static final DateTimeFormatter SUBJECT_DATE = DateTimeFormatter.ofPattern("EEE, d MMM", Locale.ENGLISH);
    private static final DateTimeFormatter HEADER_DATE = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH);

    private static final String GREEN = "#34d399";
    private static final String RED = "#f87171";
    private static final String AMBER = "#fbbf24";
    private static final String GREY = "#9ca3af";

    private final JavaMailSender mailSender;
    private final ITemplateEngine templateEngine;
    private final UnsubscribeTokenService unsubscribeTokens;
    private final DigestProperties properties;

    public void send(User user, List<StockDigestItem> items, LocalDate date) {
        String unsubscribeUrl = unsubscribeUrl(user);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(properties.from());
            helper.setTo(user.getEmail());
            helper.setSubject(subject(date));
            helper.setText(plainText(user, items, date, unsubscribeUrl), render(user, items, date, unsubscribeUrl));
            message.setHeader("List-Unsubscribe", "<" + unsubscribeUrl + ">");
            message.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new MailPreparationException("Could not build digest for user " + user.getId(), e);
        }
    }

    static String subject(LocalDate date) {
        return "Your MarketSense digest — " + date.format(SUBJECT_DATE);
    }

    String unsubscribeUrl(User user) {
        return properties.baseUrl() + "/api/digest/unsubscribe?uid=" + user.getId() + "&sig=" + unsubscribeTokens.sign(user.getId());
    }

    String render(User user, List<StockDigestItem> items, LocalDate date, String unsubscribeUrl) {
        Context context = new Context(Locale.ENGLISH);
        context.setVariable("subject", subject(date));
        context.setVariable("dateLabel", date.format(HEADER_DATE));
        context.setVariable("firstName", user.getFirstName());
        context.setVariable("rows", items.stream().map(this::toRow).toList());
        context.setVariable("preferencesUrl", properties.frontendUrl() + "/profile");
        context.setVariable("watchlistUrl", properties.frontendUrl() + "/watchlists");
        context.setVariable("unsubscribeUrl", unsubscribeUrl);
        return templateEngine.process("email/digest", context);
    }

    String plainText(User user, List<StockDigestItem> items, LocalDate date, String unsubscribeUrl) {
        StringBuilder text = new StringBuilder("MarketSense daily digest — " + date.format(HEADER_DATE) + "\n\n");
        text.append("Good morning").append(user.getFirstName() == null ? "" : ", " + user.getFirstName()).append(".\n\n");
        for (StockDigestItem item : items) {
            DigestEmailRow row = toRow(item);
            text.append(row.getSymbol()).append("  ").append(row.getPriceText()).append("  ").append(row.getChangeText()).append('\n');
            if (row.getSentiment() != null) {
                text.append("  ").append(row.getSentiment()).append(" (").append(row.getConfidenceText()).append("): ").append(row.getCatalyst()).append('\n');
            }
            row.getHeadlines().forEach(h -> text.append("  - ").append(h.title()).append('\n'));
            text.append('\n');
        }
        text.append("AI insights are generated from recent headlines and may be inaccurate. Not financial advice.\n");
        text.append("Unsubscribe: ").append(unsubscribeUrl).append('\n');
        return text.toString();
    }

    DigestEmailRow toRow(StockDigestItem item) {
        MarketInsightDto insight = item.insight();
        String sentiment = insight == null ? null : insight.sentiment();
        return new DigestEmailRow(
                item.symbol(),
                item.name() == null ? "" : item.name(),
                properties.frontendUrl() + "/stock/" + item.symbol(),
                formatPrice(item.price()),
                formatChange(item.change(), item.percentChange()),
                changeColor(item.change()),
                sentiment,
                sentimentColor(sentiment),
                insight == null ? null : "Confidence " + Math.round(insight.confidence() * 100) + "%",
                insight == null ? null : insight.catalyst(),
                item.headlines() == null ? List.of() : item.headlines());
    }

    static String formatPrice(Double price) {
        return price == null ? "—" : NumberFormat.getCurrencyInstance(Locale.US).format(price);
    }

    static String formatChange(Double change, Double percentChange) {
        if (change == null || percentChange == null) return "Price unavailable";
        String arrow = change >= 0 ? "▲" : "▼";
        String sign = change >= 0 ? "+" : "";
        return String.format(Locale.US, "%s %s%.2f (%s%.2f%%)", arrow, sign, change, sign, percentChange);
    }

    static String changeColor(Double change) {
        if (change == null) return GREY;
        return change >= 0 ? GREEN : RED;
    }

    static String sentimentColor(String sentiment) {
        if (sentiment == null) return GREY;
        return switch (sentiment) {
            case "BULLISH" -> GREEN;
            case "BEARISH" -> RED;
            default -> AMBER;
        };
    }
}
