package com.socialwebapp.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @GetMapping("/ping")
    public PingResponse ping() {
        return new PingResponse("ok");
    }

    @PostMapping("/echo")
    public String echo(@RequestBody String raw) {
        return raw;
    }

    @PostMapping("/login")
    public TokenService.TokenResponse login(@Valid @RequestBody LoginRequest req) {
        log.info("Login attempt for email={}", req.email());

        final Authentication auth;
        try {
            auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
            log.info("Login auth OK for email={} principalType={} authorities={}",
                    req.email(),
                    auth.getPrincipal() != null ? auth.getPrincipal().getClass().getName() : "null",
                    auth.getAuthorities()
            );
        } catch (org.springframework.security.core.AuthenticationException ex) {
            log.warn("Login auth FAILED for email={} authExType={} msg={}",
                    req.email(),
                    ex.getClass().getSimpleName(),
                    ex.getMessage()
            );
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid_credentials");
        }

        try {
            var token = tokenService.issue(auth);
            log.info("Token issued OK for email={}", req.email());
            return token;
        } catch (Exception ex) {
            log.error("Token issue FAILED for email={} exType={} msg={}",
                    req.email(),
                    ex.getClass().getName(),
                    ex.getMessage(),
                    ex
            );
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "token_issue_failed");
        }
    }



    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        return new MeResponse(auth.getName());
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record MeResponse(String subject) {}
    public record PingResponse(String status) {}
}
