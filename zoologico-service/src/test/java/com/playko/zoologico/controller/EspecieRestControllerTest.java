package com.playko.zoologico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.WebSecurityConfig;
import com.playko.zoologico.configuration.security.jwt.AuthEntryPointJwt;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetailsService;
import com.playko.zoologico.dto.request.EspecieRequestDto;
import com.playko.zoologico.dto.response.AnimalesPorEspecieResponseDto;
import com.playko.zoologico.dto.response.EspecieResponseDto;
import com.playko.zoologico.service.IEspecieService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = EspecieRestController.class)
@Import(WebSecurityConfig.class) // Importamos tu configuración de seguridad real
class EspecieRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IEspecieService especieService; // Mock del service del controlador

    // Mocks necesarios para inyectar WebSecurityConfig
    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    private static final String BASE_URL = "/api/especies";

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void obtenerEspeciePorId_Admin_RetornaOk() throws Exception {
        EspecieResponseDto dto = new EspecieResponseDto();
        dto.setId(1L);
        dto.setNombre("Tigre");

        when(especieService.obtenerEspeciePorId(1L)).thenReturn(dto);

        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tigre"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void obtenerEspeciePorId_UsuarioSinPermiso_RetornaForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL + "/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void crearEspecie_Admin_RetornaCreated() throws Exception {
        EspecieRequestDto dto = new EspecieRequestDto();
        dto.setNombre("León");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.Mensaje").value("Especie creada correctamente."));

        verify(especieService, times(1)).crearEspecie(any(EspecieRequestDto.class));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void crearEspecie_UsuarioSinPermiso_RetornaForbidden() throws Exception {
        EspecieRequestDto dto = new EspecieRequestDto();
        dto.setNombre("León");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void editarEspecie_Admin_RetornaOk() throws Exception {
        EspecieRequestDto dto = new EspecieRequestDto();
        dto.setNombre("León");

        mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mensaje").value("Especie actualizada correctamente."));

        verify(especieService, times(1)).editarEspecie(eq(1L), any(EspecieRequestDto.class));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void eliminarEspecie_Admin_RetornaOk() throws Exception {
        mockMvc.perform(delete(BASE_URL + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mensaje").value("Especie eliminada correctamente."));

        verify(especieService, times(1)).eliminarEspecie(1L);
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void obtenerTodasLasEspecies_Admin_RetornaOk() throws Exception {
        EspecieResponseDto dto = new EspecieResponseDto();
        dto.setId(1L);
        dto.setNombre("Tigre");
        when(especieService.obtenerTodasLasEspecies()).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Tigre"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void obtenerCantidadAnimalesPorEspecie_Admin_RetornaOk() throws Exception {
        AnimalesPorEspecieResponseDto dto = new AnimalesPorEspecieResponseDto();
        dto.setEspecie("Tigre");
        dto.setCantidadAnimales(5);

        when(especieService.obtenerCantidadAnimalesPorEspecie()).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE_URL + "/indicador/animalesPorEspecie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especie").value("Tigre"))
                .andExpect(jsonPath("$[0].cantidadAnimales").value(5));
    }
}
