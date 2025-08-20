package com.playko.zoologico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.WebSecurityConfig;
import com.playko.zoologico.configuration.security.jwt.AuthEntryPointJwt;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetailsService;
import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.CantidadAnimalesPorZonaResponseDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.service.IZonaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ZonaRestController.class)
@Import(WebSecurityConfig.class)
class ZonaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IZonaService zonaService;

    // Mocks necesarios para inyectar WebSecurityConfig
    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    private static final String BASE_URL = "/api/zonas";

    // ----------- GET /{id} -----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void obtenerZonaPorId_Admin_RetornaOk() throws Exception {
        ZonaResponseDto dto = new ZonaResponseDto();
        dto.setNombre("Zona A");

        when(zonaService.obtenerZonaPorId(1L)).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Zona A"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void obtenerZonaPorId_SinPermiso_RetornaForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isForbidden());
    }

    // ----------- GET / -----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void listarZonas_Admin_RetornaOk() throws Exception {
        ZonaResponseDto dto = new ZonaResponseDto();
        dto.setNombre("Zona A");

        Page<ZonaResponseDto> page = new PageImpl<>(List.of(dto));
        when(zonaService.obtenerTodasLasZonas(null, 0)).thenReturn(page);

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Zona A"));
    }

    // ----------- POST / -----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void crearZona_Admin_RetornaCreated() throws Exception {
        ZonaRequestDto request = new ZonaRequestDto();
        request.setNombre("Zona Nueva");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.Mensaje").value("Zona creada exitosamente."));

        verify(zonaService, times(1)).crearZona(any(ZonaRequestDto.class));
    }

    // ----------- PUT /{id} -----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void editarZona_Admin_RetornaOk() throws Exception {
        ZonaRequestDto request = new ZonaRequestDto();
        request.setNombre("Zona Editada");

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mensaje").value("Zona actualizada exitosamente."));

        verify(zonaService, times(1)).editarZona(eq(1L), any(ZonaRequestDto.class));
    }

    // ----------- DELETE /{id} -----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void eliminarZona_Admin_RetornaOk() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mensaje").value("Zona eliminada exitosamente."));

        verify(zonaService, times(1)).eliminarZona(1L);
    }

    // ----------- GET /indicador/cantidadAnimalesPorZona -----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void obtenerCantidadAnimalesPorZona_Admin_RetornaOk() throws Exception {
        CantidadAnimalesPorZonaResponseDto dto = new CantidadAnimalesPorZonaResponseDto();
        dto.setNombreZona("Zona A");
        dto.setCantidadAnimales(5L);

        when(zonaService.obtenerCantidadAnimalesPorZona()).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE_URL + "/indicador/cantidadAnimalesPorZona"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreZona").value("Zona A"))
                .andExpect(jsonPath("$[0].cantidadAnimales").value(5));
    }
}
