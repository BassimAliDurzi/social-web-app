package com.socialwebapp.api.feed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FeedControllerTests {

    @LocalServerPort
    int port;

    private final ObjectMapper om = new ObjectMapper();
    private final HttpClient client = HttpClient.newHttpClient();

    @Test
    void postFeed_withoutToken_returns401() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/feed"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"content":"Hello"}
                        """))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(401, res.statusCode());
    }

    @Test
    void getFeed_pageLessThanOne_returns400() throws Exception {
        String token = loginAndGetAccessToken("user@example.com", "Password123!");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/feed?page=0&limit=10"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, res.statusCode());
    }

    @Test
    void postFeed_withToken_createsItem_andGetReturnsIt() throws Exception {
        String token = loginAndGetAccessToken("user@example.com", "Password123!");
        String uniqueContent = "Hello IT " + UUID.randomUUID();

        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(uri("/api/feed"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .header("Authorization", "Bearer " + token)
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"content":"%s"}
                        """.formatted(uniqueContent)))
                .build();

        HttpResponse<String> postRes = client.send(postReq, HttpResponse.BodyHandlers.ofString());

        System.out.println("POST /api/feed status=" + postRes.statusCode());
        System.out.println("POST /api/feed body=" + postRes.body());

        assertEquals(
                201,
                postRes.statusCode(),
                "POST /api/feed expected 201 but got %s, body=%s".formatted(postRes.statusCode(), postRes.body())
        );

        JsonNode created = om.readTree(postRes.body());
        assertTrue(created.hasNonNull("id"));
        assertEquals("post", created.get("kind").asText());
        assertEquals(uniqueContent, created.get("content").asText());

        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(uri("/api/feed?page=1&limit=10"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> getRes = client.send(getReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getRes.statusCode());

        System.out.println("GET /api/feed status=" + getRes.statusCode());
        System.out.println("GET /api/feed body=" + getRes.body());

        JsonNode feed = om.readTree(getRes.body());
        assertTrue(feed.get("items").isArray());

        boolean found = false;
        for (JsonNode item : feed.get("items")) {
            if (uniqueContent.equals(item.get("content").asText())) {
                found = true;
                break;
            }
        }
        assertTrue(found, "GET /api/feed did not include created item content=" + uniqueContent);
    }

    private String loginAndGetAccessToken(String email, String password) throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/auth/login"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)))
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, res.statusCode());

        JsonNode root = om.readTree(res.body());
        return root.get("accessToken").asText();
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}
