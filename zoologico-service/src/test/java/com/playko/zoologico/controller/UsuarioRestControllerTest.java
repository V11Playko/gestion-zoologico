package com.playko.zoologico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.WebSecurityConfig;
import com.playko.zoologico.configuration.security.jwt.AuthEntryPointJwt;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetailsService;
import com.playko.zoologico.dto.request.UsuarioRequestDto;
import com.playko.zoologico.dto.response.UsuarioResponseDto;
import com.playko.zoologico.service.IUsuarioService;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = UsuarioRestController.class)
@Import(WebSecurityConfig.class) // Importa tu configuración de seguridad real
class UsuarioRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private IUsuarioService usuarioService;

    // Mocks necesarios para inyectar WebSecurityConfig
    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    private final static String BASE_URL = "/api/usuarios";

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void listarUsuarios_Admin_RetornaOk() throws Exception {
        UsuarioResponseDto dto = new UsuarioResponseDto();
        dto.setId(1L);
        dto.setEmail("admin@test.com");

        when(usuarioService.listarUsuarios()).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("admin@test.com"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void listarUsuarios_UsuarioSinPermiso_RetornaForbidden() throws Exception {
        mockMvc.perform(get(BASE_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void crearUsuarioEmpleado_Admin_RetornaCreated() throws Exception {
        UsuarioRequestDto dto = new UsuarioRequestDto();
        dto.setEmail("empleado@test.com");
        dto.setPassword("123456");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.Mensaje").value("Usuario creado correctamente."));

        verify(usuarioService, times(1)).crearUsuarioEmpleado(any(UsuarioRequestDto.class));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_USER"})
    void crearUsuarioEmpleado_UsuarioSinPermiso_RetornaForbidden() throws Exception {
        UsuarioRequestDto dto = new UsuarioRequestDto();
        dto.setEmail("empleado@test.com");
        dto.setPassword("123456");

        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registrarCliente_Publico_RetornaCreated() throws Exception {
        UsuarioRequestDto dto = new UsuarioRequestDto();
        dto.setEmail("cliente@test.com");
        dto.setPassword("123456");

        mockMvc.perform(post(BASE_URL + "/registro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.Mensaje").value("Usuario creado correctamente."));

        verify(usuarioService, times(1)).crearUsuarioCliente(any(UsuarioRequestDto.class));
    }
}
