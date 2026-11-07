package com.playko.zoologico.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.jwt.AuthEntryPointJwt;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AuthEntryPointJwtTest {

    private final AuthEntryPointJwt entryPoint = new AuthEntryPointJwt();
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void commence_shouldReturn401WithJsonBody() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setServletPath("/api/animales");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException ex = new BadCredentialsException("Invalid credentials");

        entryPoint.commence(request, response, ex);

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");

        @SuppressWarnings("unchecked")
        Map<String, Object> body = mapper.readValue(response.getContentAsString(), Map.class);
        assertThat(body.get("status")).isEqualTo(401);
        assertThat(body.get("error")).isEqualTo("Unauthorized");
        assertThat(body.get("message")).isEqualTo("Invalid credentials");
        assertThat(body.get("path")).isEqualTo("/api/animales");
    }
}
