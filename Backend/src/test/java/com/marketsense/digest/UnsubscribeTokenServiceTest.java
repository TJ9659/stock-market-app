package com.marketsense.digest;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UnsubscribeTokenServiceTest {

    private final UnsubscribeTokenService tokens = new UnsubscribeTokenService("test-secret");

    @Test
    void signatureVerifiesForTheSameUser() {
        assertThat(tokens.verify(42L, tokens.sign(42L))).isTrue();
    }

    @Test
    void signatureCannotBeReusedForAnotherUser() {
        assertThat(tokens.verify(43L, tokens.sign(42L))).isFalse();
    }

    @Test
    void tamperedOrMissingSignatureIsRejected() {
        String sig = tokens.sign(42L);
        String tampered = (sig.charAt(0) == 'A' ? 'B' : 'A') + sig.substring(1);

        assertThat(tokens.verify(42L, tampered)).isFalse();
        assertThat(tokens.verify(42L, "")).isFalse();
        assertThat(tokens.verify(42L, null)).isFalse();
        assertThat(tokens.verify(null, sig)).isFalse();
    }

    @Test
    void differentSecretProducesDifferentSignature() {
        assertThat(new UnsubscribeTokenService("other-secret").verify(42L, tokens.sign(42L))).isFalse();
    }

    @Test
    void signatureIsUrlSafe() {
        assertThat(tokens.sign(42L)).matches("[A-Za-z0-9_-]+");
    }
}
