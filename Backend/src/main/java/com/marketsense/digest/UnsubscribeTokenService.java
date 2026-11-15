package com.marketsense.digest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.util.Base64;

@Component
public class UnsubscribeTokenService {

    private final SecretKeySpec key;

    public UnsubscribeTokenService(@Value("${security.jwt.secret-key}") String secret) {
        this.key = new SecretKeySpec(("digest-unsubscribe:" + secret).getBytes(StandardCharsets.UTF_8), "HmacSHA256");
    }

    public String sign(long userId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(key);
            byte[] signature = mac.doFinal(("unsubscribe:" + userId).getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean verify(Long userId, String signature) {
        if (userId == null || signature == null || signature.isBlank()) {
            return false;
        }
        return MessageDigest.isEqual(sign(userId).getBytes(StandardCharsets.UTF_8), signature.getBytes(StandardCharsets.UTF_8));
    }
}
