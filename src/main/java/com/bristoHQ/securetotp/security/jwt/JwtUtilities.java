package com.bristoHQ.securetotp.security.jwt;

import io.jsonwebtoken.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.bristoHQ.securetotp.repositories.BlacklistedTokenRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtilities {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Autowired
    private BlacklistedTokenRepository blacklistedTokenRepository;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String email = extractUsername(token);
        return (email.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String email, List<String> roles) {

        return Jwts.builder().setSubject(email).claim("role", roles).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(Date.from(Instant.now().plus(jwtExpiration, ChronoUnit.MILLIS)))
                .signWith(SignatureAlgorithm.HS256, secret).compact();
    }

    public String generateResetToken(String email) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 15 * 60 * 1000); // 15 minutes

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
    public String validateResetToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            String email = claims.getSubject();
            Date expiration = claims.getExpiration();

            if (expiration.before(new Date())) {
                log.info("Reset token has expired.");
                return null; // Token is expired
            }
            return email; // Valid token, return the email
        } catch (JwtException e) {
            log.info("Invalid reset token: {}", e.getMessage());
            return null; // Invalid token
        }
    }

    public String generateTokenWithoutExpiration(String email, List<String> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", roles)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                // No expiration date set
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public boolean validateToken(String token) {
        if (blacklistedTokenRepository.existsByToken(token)) {
            return false;
        }
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.info("Invalid JWT signature.");
            log.trace("Invalid JWT signature trace: {}", e);
        } catch (MalformedJwtException e) {
            log.info("Invalid JWT token.");
            log.trace("Invalid JWT token trace: {}", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT token.");
            log.trace("Expired JWT token trace: {}", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT token.");
            log.trace("Unsupported JWT token trace: {}", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT token compact of handler are invalid.");
            log.trace("JWT token compact of handler are invalid trace: {}", e);
        }
        return false;
    }

    public String getToken(HttpServletRequest httpServletRequest) {
        final String bearerToken = httpServletRequest.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7, bearerToken.length());
        } // The part after "Bearer "
        return null;
    }
}
