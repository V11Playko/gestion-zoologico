package com.playko.zoologico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.WebSecurityConfig;
import com.playko.zoologico.configuration.security.jwt.AuthEntryPointJwt;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetailsService;
import com.playko.zoologico.service.IExcelService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ExcelRestController.class)
@Import(WebSecurityConfig.class)
class ExcelRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IExcelService excelService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;



    // -----------------------------------------
    // Test GET: generarExcelComentariosPorFecha
    // -----------------------------------------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void generarExcelComentariosPorFecha_RetornaCreated() throws Exception {
        byte[] mockBytes = new byte[]{1,2,3};
        when(excelService.generarExcelComentariosPorFecha("2025-08-18")).thenReturn(mockBytes);

        mockMvc.perform(get("/api/comentarios/excel")
                        .param("fecha", "2025-08-18")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comentarios-2025-08-18.xlsx\""));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"}) // explícito ROLE_USER
    void generarExcelComentariosPorFecha_UsuarioSinRol_RetornaForbidden() throws Exception {
        mockMvc.perform(get("/api/comentarios/excel")
                        .param("fecha", "2025-08-18")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}