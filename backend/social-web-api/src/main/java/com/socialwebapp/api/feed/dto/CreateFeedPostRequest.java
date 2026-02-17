package com.socialwebapp.api.feed.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateFeedPostRequest(
        @NotBlank
        @Size(max = 1000)
        String content
) {}
