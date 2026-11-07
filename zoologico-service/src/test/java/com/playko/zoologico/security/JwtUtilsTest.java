package com.playko.zoologico.security;

import com.playko.zoologico.configuration.security.exception.JwtException;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtUtilsTest {

    // HS512 requiere mínimo 64 bytes (512 bits)
    private static final String SECRET =
            "clave-super-secreta-para-tests-con-al-menos-64-caracteres-hs512!!";
    private static final int EXPIRATION_MS = 3_600_000;

    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMillis", EXPIRATION_MS);
    }

    private Authentication buildAuth(String email, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUsername()).thenReturn(email);
        when(userDetails.getAuthorities()).thenAnswer(inv -> authorities);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        return auth;
    }

    /* ===== generateJwtToken ===== */

    @Test
    void generateJwtToken_shouldReturnNonBlankToken() {
        Authentication auth = buildAuth("user@test.com", "ROLE_ADMIN");

        String token = jwtUtils.generateJwtToken(auth);

        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // header.payload.signature
    }

    /* ===== getUserNameFromJwtToken ===== */

    @Test
    void getUserNameFromJwtToken_shouldReturnSubject() {
        String email = "subject@test.com";
        String token = jwtUtils.generateJwtToken(buildAuth(email, "ROLE_EMPLEADO"));

        String result = jwtUtils.getUserNameFromJwtToken(token);

        assertThat(result).isEqualTo(email);
    }

    /* ===== getEmailFromJwtToken ===== */

    @Test
    void getEmailFromJwtToken_shouldReturnEmail() {
        String email = "email@test.com";
        String token = jwtUtils.generateJwtToken(buildAuth(email, "ROLE_CLIENTE"));

        String result = jwtUtils.getEmailFromJwtToken(token);

        assertThat(result).isEqualTo(email);
    }

    /* ===== validateJwtToken ===== */

    @Test
    void validateJwtToken_shouldReturnTrue_forValidToken() {
        String token = jwtUtils.generateJwtToken(buildAuth("valid@test.com", "ROLE_ADMIN"));

        boolean valid = jwtUtils.validateJwtToken(token);

        assertThat(valid).isTrue();
    }

    @Test
    void validateJwtToken_shouldThrowJwtException_forMalformedToken() {
        assertThrows(JwtException.class,
                () -> jwtUtils.validateJwtToken("este.no.es.un.jwt.valido"));
    }

    @Test
    void validateJwtToken_shouldThrowJwtException_forEmptyToken() {
        assertThrows(JwtException.class,
                () -> jwtUtils.validateJwtToken(""));
    }

    @Test
    void validateJwtToken_shouldThrowJwtException_forExpiredToken() {
        // Genera token que ya expiró
        JwtUtils expiredJwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(expiredJwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(expiredJwtUtils, "jwtExpirationMillis", -1000);

        String expiredToken = expiredJwtUtils.generateJwtToken(buildAuth("exp@test.com", "ROLE_ADMIN"));

        assertThrows(JwtException.class, () -> jwtUtils.validateJwtToken(expiredToken));
    }
}
