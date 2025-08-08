package com.bristoHQ.securetotp.scheduling;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.bristoHQ.securetotp.repositories.BlacklistedTokenRepository;
import com.bristoHQ.securetotp.repositories.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final BlacklistedTokenRepository blacklistedTokenRepository;

    private final UserRepository userRepository;

    @Scheduled(cron = "0 30 0 * * ?") // Runs at 00:30 AM every day
    public void deleteUnverifiedUsers() {
        Date cutoff = Date.from(Instant.now().minus(30, ChronoUnit.DAYS));
        userRepository.deleteAll(
                userRepository.findAll().stream()
                        .filter(user -> !user.isVerified() && user.getAccountCreatedAt().before(cutoff))
                        .toList());
    }

    @Scheduled(cron = "0 0 0 * * ?") // Runs at midnight every day
    public void cleanupExpiredTokens() {
        blacklistedTokenRepository.deleteAll(
                blacklistedTokenRepository.findAll().stream()
                        .filter(token -> token.getExpirationDate().isBefore(Instant.now()))
                        .toList());
    }
}
