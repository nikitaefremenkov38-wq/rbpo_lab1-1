package com.example.demo.security;

import com.example.demo.entity.AppUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private final SecretKey secretKey;
    private final Duration accessTokenTtl;
    private final Duration refreshTokenTtl;
    private final String issuer;

    public JwtTokenProvider(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-token-ttl}") Duration accessTokenTtl,
            @Value("${security.jwt.refresh-token-ttl}") Duration refreshTokenTtl,
            @Value("${security.jwt.issuer}") String issuer
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenTtl = accessTokenTtl;
        this.refreshTokenTtl = refreshTokenTtl;
        this.issuer = issuer;
    }

    public String generateAccessToken(AppUser user, Long sessionId) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream().map(Enum::name).toList();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(accessTokenTtl)))
                .setId(UUID.randomUUID().toString())
                .claim("type", "access")
                .claim("userId", user.getId())
                .claim("roles", roles)
                .claim("sessionId", sessionId)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(AppUser user, String refreshTokenId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .setSubject(user.getUsername())
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(refreshTokenTtl)))
                .setId(refreshTokenId)
                .claim("type", "refresh")
                .claim("userId", user.getId())
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseAccessToken(String token) {
        Claims claims = parseToken(token).getBody();
        validateTokenType(claims, "access");
        return claims;
    }

    public Claims parseRefreshToken(String token) {
        Claims claims = parseToken(token).getBody();
        validateTokenType(claims, "refresh");
        return claims;
    }

    public Duration getAccessTokenTtl() {
        return accessTokenTtl;
    }

    public Duration getRefreshTokenTtl() {
        return refreshTokenTtl;
    }

    private Jws<Claims> parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(token);
        } catch (JwtException ex) {
            throw ex;
        }
    }

    private void validateTokenType(Claims claims, String expectedType) {
        Object type = claims.get("type");
        if (!expectedType.equals(type)) {
            throw new JwtException("Invalid token type");
        }
    }
}
