package com.helpdesk.security;

import com.helpdesk.domain.enums.Role;
import com.helpdesk.domain.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("HelpDeskSuperSecretKeyForHS256MustBeAtLeast256BitsLong!!");
        properties.setExpirationMs(3600000);
        jwtService = new JwtService(properties);
    }

    @Test
    void generateAndParseToken() {
        User user = new User();
        user.setId(7L);
        user.setName("Admin");
        user.setEmail("admin@helpdesk.local");
        user.setRole(Role.ADMIN);

        String token = jwtService.generateToken(user);

        assertNotNull(token);
        assertEquals("admin@helpdesk.local", jwtService.extractEmail(token));
        assertEquals("ADMIN", jwtService.parse(token).get("role", String.class));
        assertEquals(7L, jwtService.parse(token).get("uid", Number.class).longValue());
    }
}
