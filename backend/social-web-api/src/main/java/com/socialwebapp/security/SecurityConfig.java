package com.socialwebapp.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import java.time.Clock;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;

import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import org.springframework.security.web.SecurityFilterChain;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;


@Configuration
public class SecurityConfig {

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    SecretKey jwtSecretKey(@Value("${security.jwt.secret}") String secret) {
        // HS256 requires 256-bit key => 32 bytes minimum
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    @Bean
    JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        // In this version, SecretKeyJwtEncoderBuilder does NOT expose macAlgorithm(...)
        // The JWT header "alg" will be set when encoding (see TokenService).
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey).build();
    }

    @Bean
    JwtDecoder jwtDecoder(SecretKey jwtSecretKey) {
        // Decoder will validate using the secret key and the token's "alg" header.
        return NimbusJwtDecoder.withSecretKey(jwtSecretKey).build();
    }


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .build();
    }

    @Bean
public Clock clock() {
        return Clock.systemUTC();
        }
}
