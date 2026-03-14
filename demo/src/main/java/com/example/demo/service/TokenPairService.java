package com.example.demo.service;

import com.example.demo.dto.TokenPairResponse;
import com.example.demo.entity.AppUser;
import com.example.demo.entity.SessionStatus;
import com.example.demo.entity.UserSession;
import com.example.demo.repository.AppUserRepository;
import com.example.demo.repository.UserSessionRepository;
import com.example.demo.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class TokenPairService {

    private final AuthService authService;
    private final AppUserRepository appUserRepository;
    private final UserSessionRepository userSessionRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenPairService(
            AuthService authService,
            AppUserRepository appUserRepository,
            UserSessionRepository userSessionRepository,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.authService = authService;
        this.appUserRepository = appUserRepository;
        this.userSessionRepository = userSessionRepository;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public TokenPairResponse login(String username, String password) {
        authService.authenticate(username, password);
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        return issueTokenPair(user);
    }

    public TokenPairResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtTokenProvider.parseRefreshToken(refreshToken);
        } catch (JwtException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        String refreshTokenId = claims.getId();
        if (refreshTokenId == null || refreshTokenId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
        }

        UserSession session = userSessionRepository.findByRefreshTokenId(refreshTokenId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token not found"));

        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token revoked");
        }

        Instant now = Instant.now();
        if (session.getExpiresAt().isBefore(now)) {
            session.setStatus(SessionStatus.EXPIRED);
            session.setEndedAt(now);
            userSessionRepository.save(session);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }

        session.setStatus(SessionStatus.ROTATED);
        session.setEndedAt(now);
        userSessionRepository.save(session);

        return issueTokenPair(session.getUser());
    }

    private TokenPairResponse issueTokenPair(AppUser user) {
        Instant now = Instant.now();
        String refreshTokenId = UUID.randomUUID().toString();
        Instant refreshExpiresAt = now.plus(jwtTokenProvider.getRefreshTokenTtl());

        UserSession session = new UserSession();
        session.setUser(user);
        session.setRefreshTokenId(refreshTokenId);
        session.setStatus(SessionStatus.ACTIVE);
        session.setCreatedAt(now);
        session.setExpiresAt(refreshExpiresAt);
        session = userSessionRepository.save(session);

        String accessToken = jwtTokenProvider.generateAccessToken(user, session.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user, refreshTokenId);

        return new TokenPairResponse(accessToken, refreshToken);
    }
}
