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

import static org.junit.jupiter.api.Assertions.*;

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
    void getFeed_pageLessThanOne_returns400_contract() throws Exception {
        String token = registerAndLoginFreshUserAndGetAccessToken();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/feed?page=0&limit=10"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, res.statusCode());
        assertNotNull(res.body());
        assertFalse(res.body().isBlank());

        JsonNode root = om.readTree(res.body());
        assertTrue(root.isObject());
    }

    @Test
    void getFeed_limitTooHigh_returns400_contract() throws Exception {
        String token = registerAndLoginFreshUserAndGetAccessToken();

        HttpRequest req = HttpRequest.newBuilder()
                .uri(uri("/api/feed?page=1&limit=999"))
                .header("Authorization", "Bearer " + token)
                .GET()
                .build();

        HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, res.statusCode());
        assertNotNull(res.body());
        assertFalse(res.body().isBlank());

        JsonNode root = om.readTree(res.body());
        assertTrue(root.isObject());
    }

    @Test
    void postFeed_withToken_createsItem_andReturnsLocationHeader() throws Exception {
        String token = registerAndLoginFreshUserAndGetAccessToken();
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

        assertEquals(201, postRes.statusCode());

        String location = postRes.headers().firstValue("Location").orElse(null);
        assertNotNull(location);
        assertFalse(location.isBlank());

        JsonNode created = om.readTree(postRes.body());
        assertTrue(created.hasNonNull("id"));
        assertEquals("post", created.get("kind").asText());
        assertEquals(uniqueContent, created.get("content").asText());
    }

    /**
     * Production-grade test strategy:
     * - Each call uses a brand new user (unique email)
     * - Avoids shared state / duplicate registration / flaky auth behavior
     */
    private String registerAndLoginFreshUserAndGetAccessToken() throws Exception {
        String email = "user+" + UUID.randomUUID() + "@example.com";
        String password = "Password123!";

        // 1) Register fresh user
        HttpRequest registerReq = HttpRequest.newBuilder()
                .uri(uri("/api/auth/register"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)))
                .build();

        HttpResponse<String> registerRes = client.send(registerReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, registerRes.statusCode(), "Register should return 200. Body=" + registerRes.body());

        // 2) Login
        HttpRequest loginReq = HttpRequest.newBuilder()
                .uri(uri("/api/auth/login"))
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofString("""
                        {"email":"%s","password":"%s"}
                        """.formatted(email, password)))
                .build();

        HttpResponse<String> loginRes = client.send(loginReq, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, loginRes.statusCode(), "Login should return 200. Body=" + loginRes.body());

        JsonNode root = om.readTree(loginRes.body());
        JsonNode tokenNode = root.get("accessToken");
        assertNotNull(tokenNode, "Response must contain accessToken. Body=" + loginRes.body());

        String token = tokenNode.asText();
        assertFalse(token.isBlank(), "accessToken must not be blank");
        return token;
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }
}