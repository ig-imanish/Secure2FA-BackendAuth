package com.bristoHQ.securetotp.scheduling;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bristoHQ.securetotp.repositories.BlacklistedTokenRepository;

import java.time.Instant;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Runs at midnight every day
    public void cleanupExpiredTokens() {
        blacklistedTokenRepository.deleteAll(
                blacklistedTokenRepository.findAll().stream()
                        .filter(token -> token.getExpirationDate().isBefore(Instant.now()))
                        .toList());
    }
}
