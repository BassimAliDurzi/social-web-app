package com.socialwebapp.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank
        @JsonAlias({"email", "username", "userName"})
        String email,

        @NotBlank
        String password
) {}
