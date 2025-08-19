package com.playko.zoologico.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.WebSecurityConfig;
import com.playko.zoologico.configuration.security.jwt.AuthEntryPointJwt;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetailsService;
import com.playko.zoologico.dto.response.BusquedaResultadoDto;
import com.playko.zoologico.service.IBusquedaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(BusquedaRestController.class)
@Import(WebSecurityConfig.class)
class BusquedaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IBusquedaService busquedaService; // Mock del servicio

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    @Test
    @WithMockUser(roles = {"ADMIN"}) // Usuario simulado con ROLE_ADMIN
    void buscarPorPalabra_RetornaOk() throws Exception {
        // Preparar datos de prueba
        String palabra = "leon";
        BusquedaResultadoDto dto = new BusquedaResultadoDto();
        dto.setAnimalNombre("Leon");
        List<BusquedaResultadoDto> resultados = List.of(dto);

        // Configurar el mock del servicio
        when(busquedaService.buscarPorPalabra(palabra)).thenReturn(resultados);

        mockMvc.perform(get("/api/busqueda")
                        .param("palabra", palabra)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].animalNombre").value("Leon"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void buscarPorPalabra_RetornaNoContent() throws Exception {
        String palabra = "jirafa";

        when(busquedaService.buscarPorPalabra(palabra)).thenReturn(List.of());

        mockMvc.perform(get("/api/busqueda")
                        .param("palabra", palabra)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_CLIENTE"})
    void buscarPorPalabra_RetornaForbidden() throws Exception {
        mockMvc.perform(get("/api/busqueda")
                        .param("palabra", "leon")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden()); // Porque no tiene ROLE_ADMIN
    }
}
