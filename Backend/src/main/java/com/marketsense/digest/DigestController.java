package com.marketsense.digest;

import com.marketsense.user.User;
import com.marketsense.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/digest")
@RequiredArgsConstructor
public class DigestController {

    private final DigestService digestService;
    private final UnsubscribeTokenService unsubscribeTokens;
    private final UserRepository userRepository;
    private final DigestProperties properties;

    @RequestMapping(value = "/unsubscribe", method = {RequestMethod.GET, RequestMethod.POST}, produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> unsubscribe(@RequestParam(required = false) Long uid,
                                              @RequestParam(required = false) String sig) {
        if (!unsubscribeTokens.verify(uid, sig)) {
            return ResponseEntity.badRequest().body(page("Invalid link", "This unsubscribe link is invalid. You can turn the digest off from your profile."));
        }

        userRepository.findById(uid).filter(User::isDigestEnabled).ifPresent(user -> {
            user.setDigestEnabled(false);
            userRepository.save(user);
        });

        return ResponseEntity.ok(page("You're unsubscribed", "You won't get the daily digest anymore. You can turn it back on from your profile."));
    }

    @PostMapping("/test")
    public Map<String, Object> sendTestDigest(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        int stocks = digestService.sendToUser(user.getId());
        return Map.of("sent", true, "email", user.getEmail(), "stocks", stocks);
    }

    private String page(String title, String message) {
        return """
                <!DOCTYPE html>
                <html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1"><title>%s · MarketSense</title></head>
                <body style="margin:0;min-height:100vh;display:flex;align-items:center;justify-content:center;background:#0b0e11;font-family:-apple-system,'Segoe UI',Roboto,Arial,sans-serif;color:#e5e7eb;">
                  <div style="max-width:420px;margin:16px;padding:32px;background:#161616;border:1px solid #27272a;border-radius:16px;text-align:center;">
                    <div style="font-size:20px;font-weight:900;color:#fff;">Market<span style="color:#10b981;">Sense</span></div>
                    <h1 style="font-size:22px;color:#fff;margin:20px 0 8px;">%s</h1>
                    <p style="font-size:14px;line-height:1.5;color:#9ca3af;margin:0 0 24px;">%s</p>
                    <a href="%s/profile" style="display:inline-block;padding:10px 18px;border-radius:10px;background:#10b981;color:#000;font-weight:700;text-decoration:none;">Manage preferences</a>
                  </div>
                </body></html>
                """.formatted(title, title, message, properties.frontendUrl());
    }
}
