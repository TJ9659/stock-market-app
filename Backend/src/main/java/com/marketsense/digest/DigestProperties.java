package com.marketsense.digest;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "digest")
public record DigestProperties(
        boolean enabled,
        String cron,
        String zone,
        String from,
        String baseUrl,
        String frontendUrl,
        long symbolDelayMs
) {}
