package com.playko.zoologico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.WebSecurityConfig;
import com.playko.zoologico.configuration.security.jwt.AuthEntryPointJwt;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetailsService;
import com.playko.zoologico.dto.request.ComentarioRequestDto;
import com.playko.zoologico.dto.response.ComentarioResponseDto;
import com.playko.zoologico.dto.response.PorcentajeComentariosConRespuestasDto;
import com.playko.zoologico.service.IComentarioService;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(ComentarioRestController.class)
@Import(WebSecurityConfig.class)
class ComentarioRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IComentarioService comentarioService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private CustomUserDetailsService userDetailsService;

    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;

    // ------------------------------
    // Test POST: agregarComentario
    // ------------------------------
    @Test
    @WithMockUser(authorities = {"ROLE_CLIENTE"})
    void agregarComentario_RetornaCreated() throws Exception {
        ComentarioRequestDto requestDto = new ComentarioRequestDto();
        requestDto.setAnimalId(1L);
        requestDto.setContenido("Comentario de prueba");

        mockMvc.perform(post("/api/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.Mensaje").value("Comentario agregado correctamente."));

        verify(comentarioService).agregarComentario(any(ComentarioRequestDto.class));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ANON"})
    void agregarComentario_UsuarioSinRol_RetornaForbidden() throws Exception {
        ComentarioRequestDto requestDto = new ComentarioRequestDto();
        requestDto.setAnimalId(1L);
        requestDto.setContenido("Comentario de prueba");

        mockMvc.perform(post("/api/comentarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------
    // Test GET: obtenerMuroDeAnimal
    // -----------------------------------------
    @Test
    @WithMockUser(authorities = {"ROLE_EMPLEADO"})
    void obtenerMuroDeAnimal_RetornaOk() throws Exception {
        Long animalId = 1L;

        // Crear instancia usando constructor por defecto y setters
        ComentarioResponseDto comentario = new ComentarioResponseDto();
        comentario.setContenido("Hola");

        List<ComentarioResponseDto> comentarios = List.of(comentario);

        when(comentarioService.obtenerMuroDeAnimal(animalId)).thenReturn(comentarios);

        mockMvc.perform(get("/api/comentarios/muro/{animalId}", animalId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contenido").value("Hola"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_CLIENTE"})
    void obtenerMuroDeAnimal_UsuarioSinRol_RetornaForbidden() throws Exception {
        mockMvc.perform(get("/api/comentarios/muro/{animalId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------
    // Test GET: obtenerPorcentajeComentariosConRespuestas
    // -----------------------------------------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void obtenerPorcentajeComentariosConRespuestas_RetornaOk() throws Exception {
        PorcentajeComentariosConRespuestasDto dto = new PorcentajeComentariosConRespuestasDto();
        dto.setPorcentaje("50,0");
        when(comentarioService.obtenerPorcentajeComentariosConRespuestas()).thenReturn(dto);

        mockMvc.perform(get("/api/comentarios/indicador/porcentajeComentariosConRespuesta")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentaje").value("50,0"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_EMPLEADO"})
    void obtenerPorcentajeComentariosConRespuestas_UsuarioSinRol_RetornaForbidden() throws Exception {
        mockMvc.perform(get("/api/comentarios/indicador/porcentajeComentariosConRespuesta")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // -----------------------------------------
    // Test GET: generarExcelComentariosPorFecha
    // -----------------------------------------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void generarExcelComentariosPorFecha_RetornaCreated() throws Exception {
        byte[] mockBytes = new byte[]{1,2,3};
        when(comentarioService.generarExcelComentariosPorFecha("2025-08-18")).thenReturn(mockBytes);

        mockMvc.perform(get("/api/comentarios/comentarios/excel")
                        .param("fecha", "2025-08-18")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"comentarios-2025-08-18.xlsx\""));
    }

    @Test
    @WithMockUser(username = "user", authorities = {"ROLE_USER"}) // explícito ROLE_USER
    void generarExcelComentariosPorFecha_UsuarioSinRol_RetornaForbidden() throws Exception {
        mockMvc.perform(get("/api/comentarios/comentarios/excel")
                        .param("fecha", "2025-08-18")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

}
