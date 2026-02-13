package com.socialwebapp.auth;

import java.time.Instant;

import com.socialwebapp.security.JwtProperties;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    public record TokenResponse(String accessToken, String tokenType) {}

    private final JwtEncoder jwtEncoder;
    private final JwtProperties props;

    public TokenService(JwtEncoder jwtEncoder, JwtProperties props) {
        this.jwtEncoder = jwtEncoder;
        this.props = props;
    }

    public TokenResponse issue(Authentication auth) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(props.issuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(props.ttlSeconds()))
                .subject(auth.getName())
                .build();

        // ✅ Critical: set HS256 in header so NimbusJwtEncoder can select the signing key
        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();

        String tokenValue = jwtEncoder
                .encode(JwtEncoderParameters.from(headers, claims))
                .getTokenValue();

        return new TokenResponse(tokenValue, "Bearer");
    }
}
