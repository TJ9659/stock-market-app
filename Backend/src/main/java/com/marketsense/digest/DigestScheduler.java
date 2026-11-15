package com.marketsense.digest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "digest", name = "enabled", havingValue = "true", matchIfMissing = true)
public class DigestScheduler {

    private final DigestService digestService;

    @Scheduled(cron = "${digest.cron}", zone = "${digest.zone}")
    public void sendDailyDigest() {
        try {
            digestService.sendToAll();
        } catch (Exception e) {
            log.error("Digest run failed", e);
        }
    }
}
