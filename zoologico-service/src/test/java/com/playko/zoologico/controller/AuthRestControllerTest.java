package com.playko.zoologico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.dto.JwtTokenResponseDto;
import com.playko.zoologico.configuration.security.dto.LoginRequestDto;
import com.playko.zoologico.service.IAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthRestController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IAuthService authHandler; // <--- mockeamos el servicio

    @Test
    void loginUser_RetornaOk() throws Exception {
        // DTO de request
        LoginRequestDto requestDto = new LoginRequestDto();
        requestDto.setEmail("usuario@test.com");
        requestDto.setPassword("password123");

        // DTO de respuesta
        JwtTokenResponseDto responseDto = new JwtTokenResponseDto();
        responseDto.setJwtToken("mocked-jwt-token");

        // Configuramos el comportamiento del mock
        when(authHandler.loginUser(any(LoginRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwtToken").value("mocked-jwt-token"));
    }
}
