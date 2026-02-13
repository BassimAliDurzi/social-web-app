package com.socialwebapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(classes = SocialWebApiApplication.class)
class SocialWebApiApplicationTests {

    @Test
    void contextLoads() {}
}
