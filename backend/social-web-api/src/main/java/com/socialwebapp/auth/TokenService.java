package com.socialwebapp.auth;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.socialwebapp.security.JwtProperties;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final JwtProperties props;

    public TokenService(JwtProperties props) {
        this.props = props;
    }

    public TokenResponse issue(Authentication auth) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(props.ttlSeconds());

        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(a -> a.replaceFirst("^ROLE_", ""))
                .toList();

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(props.issuer())
                .issueTime(Date.from(now))
                .expirationTime(Date.from(exp))
                .subject(auth.getName())
                .claim("roles", roles)
                .build();

        String token = signHs256(claims, props.secret());
        return new TokenResponse(token, "Bearer", props.ttlSeconds());
    }

    private String signHs256(JWTClaimsSet claims, String rawSecret) {
        try {
            byte[] secretBytes = rawSecret.getBytes(java.nio.charset.StandardCharsets.UTF_8);

            // HS256 requires sufficiently long secret; keep it >= 32 chars.
            MACSigner signer = new MACSigner(secretBytes);

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS256);
            SignedJWT jwt = new SignedJWT(header, claims);
            jwt.sign(signer);

            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Failed to sign JWT (HS256).", e);
        }
    }

    public record TokenResponse(String accessToken, String tokenType, long expiresIn) {}
}