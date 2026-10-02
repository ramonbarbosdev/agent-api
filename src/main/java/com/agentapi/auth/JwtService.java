package com.agentapi.auth;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.agentapi.config.AuthProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final AuthProperties authProperties;
    private final SecretKey key;

    public JwtService(AuthProperties authProperties) {
        this.authProperties = authProperties;
        this.key = Keys.hmacShaKeyFor(authProperties.getJwtSecret().getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String createToken(UUID userId, String email) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(authProperties.getJwtExpirationMinutes() * 60);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(Date.from(now))
                .expiration(Date.from(exp))
                .signWith(key)
                .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID userIdFromToken(String token) {
        return UUID.fromString(parse(token).getSubject());
    }
}
