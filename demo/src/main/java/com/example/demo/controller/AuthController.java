package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RefreshTokenRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.TokenPairResponse;
import com.example.demo.dto.UserResponse;
import com.example.demo.entity.AppUser;
import com.example.demo.service.AuthService;
import com.example.demo.service.TokenPairService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final TokenPairService tokenPairService;

    public AuthController(AuthService authService, TokenPairService tokenPairService) {
        this.authService = authService;
        this.tokenPairService = tokenPairService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = authService.register(request);
        return new UserResponse(user.getUsername(), user.getRoles().stream().map(Enum::name).collect(Collectors.toSet()));
    }

    @PostMapping("/login")
    public TokenPairResponse login(@Valid @RequestBody LoginRequest request) {
        return tokenPairService.login(request.getUsername(), request.getPassword());
    }

    @PostMapping("/refresh")
    public TokenPairResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return tokenPairService.refresh(request.getRefreshToken());
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
        return new UserResponse(authentication.getName(), roles);
    }

    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("XSRF-TOKEN", token.getToken())
                .httpOnly(false)
                .path("/")
                .sameSite("Lax")
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return Map.of("token", token.getToken());
    }
}
