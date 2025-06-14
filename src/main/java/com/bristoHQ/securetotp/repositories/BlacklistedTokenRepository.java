package com.bristoHQ.securetotp.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.bristoHQ.securetotp.models.auth.BlacklistedToken;

public interface BlacklistedTokenRepository extends MongoRepository<BlacklistedToken, String> {
    boolean existsByToken(String token);
}
