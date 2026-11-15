package com.marketsense.auth;

import com.marketsense.common.exceptions.InvalidVerificationTokenException;
import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Slf4j
@Service
public class EmailVerificationService {

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final VerificationMailer mailer;
    private final Duration ttl;
    private final Duration resendCooldown;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    @Autowired
    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository,
                                    UserRepository userRepository,
                                    VerificationMailer mailer,
                                    @Value("${app.verification.ttl-hours}") long ttlHours,
                                    @Value("${app.verification.resend-cooldown-seconds}") long resendCooldownSeconds) {
        this(tokenRepository, userRepository, mailer, Duration.ofHours(ttlHours), Duration.ofSeconds(resendCooldownSeconds), Clock.systemUTC());
    }

    EmailVerificationService(EmailVerificationTokenRepository tokenRepository, UserRepository userRepository,
                             VerificationMailer mailer, Duration ttl, Duration resendCooldown, Clock clock) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.mailer = mailer;
        this.ttl = ttl;
        this.resendCooldown = resendCooldown;
        this.clock = clock;
    }

    @Transactional
    public void createAndSend(User user) {
        tokenRepository.deleteByUser(user);

        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        Instant now = clock.instant();
        tokenRepository.save(new EmailVerificationToken(user, hash(rawToken), now, now.plus(ttl)));

        try {
            mailer.send(user, rawToken, ttl);
        } catch (Exception e) {
            log.warn("Verification email failed for user {}: {}", user.getId(), e.getMessage());
        }
    }

    @Transactional
    public void verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new InvalidVerificationTokenException("This verification link is invalid or has already been used.");
        }

        EmailVerificationToken token = tokenRepository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new InvalidVerificationTokenException("This verification link is invalid or has already been used."));

        if (token.getExpiresAt().isBefore(clock.instant())) {
            throw new InvalidVerificationTokenException("This verification link has expired. Request a new one.");
        }

        User user = token.getUser();
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.deleteByUser(user);
    }

    @Transactional
    public void resend(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmail(email.trim())
                .filter(user -> !user.isEmailVerified())
                .filter(user -> !isCoolingDown(user))
                .ifPresent(this::createAndSend);
    }

    private boolean isCoolingDown(User user) {
        Instant cutoff = clock.instant().minus(resendCooldown);
        return tokenRepository.findTopByUserOrderByCreatedAtDesc(user)
                .map(token -> token.getCreatedAt().isAfter(cutoff))
                .orElse(false);
    }

    static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
