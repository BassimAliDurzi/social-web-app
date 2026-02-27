package com.socialwebapp.auth;

import com.socialwebapp.auth.data.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RegisterService registerService;
    private final UserRepository userRepository;

    public AuthController(
            AuthenticationManager authenticationManager,
            TokenService tokenService,
            RegisterService registerService,
            UserRepository userRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.registerService = registerService;
        this.userRepository = userRepository;
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
    public ResponseEntity<TokenService.TokenResponse> login(@Valid @RequestBody LoginRequest req) {
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

            return ResponseEntity
                    .ok()
                    .header("X-Debug-TokenService", "issued")
                    .body(token);

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

    @PostMapping("/register")
    public RegisterResponse register(@Valid @RequestBody RegisterRequest req) {
        log.info("Register attempt for email={}", req.email());

        try {
            var created = registerService.register(req.email(), req.password());
            log.info("Register OK for email={} id={}", created.getEmail(), created.getId());
            return new RegisterResponse(created.getId(), created.getEmail());
        } catch (IllegalArgumentException ex) {
            // e.g. "Email already exists"
            log.warn("Register FAILED for email={} msg={}", req.email(), ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "email_already_exists");
        } catch (Exception ex) {
            log.error("Register FAILED for email={} exType={} msg={}",
                    req.email(),
                    ex.getClass().getName(),
                    ex.getMessage(),
                    ex
            );
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "register_failed");
        }
    }

    /**
     * Returns the authenticated user's identity for frontend UI rules.
     * Must include a stable user id so the SPA can resolve "my wall" and decide edit/delete visibility.
     */
    @GetMapping("/me")
    public MeResponse me(Authentication auth) {
        final String email = auth.getName(); // in our app this is the email (subject)
        var user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.warn("ME not found for email={}", email);
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user_not_found");
                });

        return new MeResponse(user.getId(), user.getEmail());
    }

    public record LoginRequest(@NotBlank String email, @NotBlank String password) {}
    public record MeResponse(Long id, String subject) {}
    public record PingResponse(String status) {}
}