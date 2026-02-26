package com.socialwebapp.auth;

/**
 * Minimal response returned after successful registration.
 */
public record RegisterResponse(
        Long id,
        String email
) {
}