package com.chat.app.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 256-bit base64 test key
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 3600000L); // 1 hour
    }

    @Test
    void testGenerateTokenAndValidate() {
        String token = jwtUtils.generateToken("prince");
        assertNotNull(token);
        assertTrue(jwtUtils.validateToken(token));
        assertEquals("prince", jwtUtils.getUsernameFromToken(token));
    }

    @Test
    void testInvalidTokenRejection() {
        assertFalse(jwtUtils.validateToken("invalid.token.string"));
    }

    @Test
    void testTamperedTokenRejection() {
        String token = jwtUtils.generateToken("prince");
        String tampered = token + "xyz";
        assertFalse(jwtUtils.validateToken(tampered));
    }
}
