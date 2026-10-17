package com.fod.fod_backend;

import com.fod.fod_backend.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FodBackendApplicationTests {

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void contextLoads() {
        // 验证 Spring 容器正常启动
    }

    @Test
    void jwtTokenWorks() {
        String token = jwtUtil.generateToken("hero");
        assertNotNull(token);
        assertTrue(token.length() > 20);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals("hero", jwtUtil.getUsernameFromToken(token));
    }

    @Test
    void expiredTokenFails() {
        assertFalse(jwtUtil.validateToken("invalid.token.here"));
    }
}
