package com.marketsense.auth;

import com.marketsense.common.exceptions.InvalidVerificationTokenException;
import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailVerificationServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-16T10:00:00Z");

    private EmailVerificationTokenRepository tokenRepository;
    private UserRepository userRepository;
    private VerificationMailer mailer;
    private EmailVerificationService service;
    private User user;

    @BeforeEach
    void setUp() {
        tokenRepository = mock(EmailVerificationTokenRepository.class);
        userRepository = mock(UserRepository.class);
        mailer = mock(VerificationMailer.class);
        service = new EmailVerificationService(tokenRepository, userRepository, mailer,
                Duration.ofHours(24), Duration.ofSeconds(60), Clock.fixed(NOW, ZoneOffset.UTC));

        user = new User("Jane", "Doe", "jane@example.com", "hashed");
        user.setId(1L);
        user.setEmailVerified(false);
    }

    @Test
    void storesOnlyTheHashAndEmailsTheRawToken() {
        service.createAndSend(user);

        ArgumentCaptor<EmailVerificationToken> saved = ArgumentCaptor.forClass(EmailVerificationToken.class);
        verify(tokenRepository).save(saved.capture());
        ArgumentCaptor<String> rawToken = ArgumentCaptor.forClass(String.class);
        verify(mailer).send(eq(user), rawToken.capture(), eq(Duration.ofHours(24)));

        assertThat(rawToken.getValue()).matches("[A-Za-z0-9_-]{43}");
        assertThat(saved.getValue().getTokenHash())
                .isNotEqualTo(rawToken.getValue())
                .isEqualTo(EmailVerificationService.hash(rawToken.getValue()));
        assertThat(saved.getValue().getExpiresAt()).isEqualTo(NOW.plus(Duration.ofHours(24)));
    }

    @Test
    void newLinkReplacesOlderOnes() {
        service.createAndSend(user);

        verify(tokenRepository).deleteByUser(user);
    }

    @Test
    void mailFailureDoesNotFailSignup() {
        doThrow(new MailSendException("SMTP down")).when(mailer).send(any(), anyString(), any());

        assertThatCode(() -> service.createAndSend(user)).doesNotThrowAnyException();
        verify(tokenRepository).save(any());
    }

    @Test
    void validTokenVerifiesTheUserAndIsConsumed() {
        when(tokenRepository.findByTokenHash(EmailVerificationService.hash("raw")))
                .thenReturn(Optional.of(new EmailVerificationToken(user, "h", NOW.minusSeconds(60), NOW.plusSeconds(3600))));

        service.verify("raw");

        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
        verify(tokenRepository).deleteByUser(user);
    }

    @Test
    void unknownOrReusedTokenIsRejected() {
        when(tokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.verify("used-or-made-up"))
                .isInstanceOf(InvalidVerificationTokenException.class)
                .hasMessageContaining("already been used");
        assertThatThrownBy(() -> service.verify(" ")).isInstanceOf(InvalidVerificationTokenException.class);
        assertThatThrownBy(() -> service.verify(null)).isInstanceOf(InvalidVerificationTokenException.class);
    }

    @Test
    void expiredTokenIsRejected() {
        when(tokenRepository.findByTokenHash(EmailVerificationService.hash("raw")))
                .thenReturn(Optional.of(new EmailVerificationToken(user, "h", NOW.minus(Duration.ofHours(25)), NOW.minusSeconds(1))));

        assertThatThrownBy(() -> service.verify("raw"))
                .isInstanceOf(InvalidVerificationTokenException.class)
                .hasMessageContaining("expired");
        assertThat(user.isEmailVerified()).isFalse();
        verify(userRepository, never()).save(any());
    }

    @Test
    void resendSendsANewLinkToUnverifiedUser() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(new EmailVerificationToken(user, "h", NOW.minusSeconds(120), NOW.plusSeconds(3600))));

        service.resend(" jane@example.com ");

        verify(mailer).send(eq(user), anyString(), any());
    }

    @Test
    void resendIsThrottled() {
        when(userRepository.findByEmail("jane@example.com")).thenReturn(Optional.of(user));
        when(tokenRepository.findTopByUserOrderByCreatedAtDesc(user))
                .thenReturn(Optional.of(new EmailVerificationToken(user, "h", NOW.minusSeconds(10), NOW.plusSeconds(3600))));

        service.resend("jane@example.com");

        verify(mailer, never()).send(any(), anyString(), any());
    }

    @Test
    void resendDoesNothingForUnknownOrAlreadyVerifiedEmails() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
        User verified = new User("Al", "Ready", "done@example.com", "hashed");
        verified.setEmailVerified(true);
        when(userRepository.findByEmail("done@example.com")).thenReturn(Optional.of(verified));

        service.resend("nobody@example.com");
        service.resend("done@example.com");
        service.resend(null);

        verify(mailer, never()).send(any(), anyString(), any());
        verify(tokenRepository, never()).save(any());
    }
}
