package com.playko.zoologico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.configuration.security.WebSecurityConfig;
import com.playko.zoologico.configuration.security.jwt.AuthEntryPointJwt;
import com.playko.zoologico.configuration.security.jwt.JwtAuthorizationFilter;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetailsService;
import com.playko.zoologico.dto.request.AnimalRequestDto;
import com.playko.zoologico.dto.response.AnimalRegistradoResponseDto;
import com.playko.zoologico.dto.response.AnimalResponseDto;
import com.playko.zoologico.service.IAnimalService;
import org.apache.catalina.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = AnimalRestController.class)
class AnimalRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IAnimalService animalService;


    // ---------- GET by ID ----------
    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLEADO"})
    void obtenerAnimalPorId_RetornaOk() throws Exception {
        Long id = 1L;
        AnimalResponseDto dto = new AnimalResponseDto();
        dto.setId(id);
        dto.setNombre("León");

        Mockito.when(animalService.obtenerAnimalPorId(id)).thenReturn(dto);

        mockMvc.perform(get("/api/animales/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nombre").value("León"));
    }

    // ---------- GET all ----------
    @Test
    @WithMockUser(roles = {"ADMIN", "EMPLEADO"})
    void obtenerTodosLosAnimales_RetornaListaOk() throws Exception {
        AnimalResponseDto dto1 = new AnimalResponseDto();
        dto1.setId(1L);
        dto1.setNombre("León");

        AnimalResponseDto dto2 = new AnimalResponseDto();
        dto2.setId(2L);
        dto2.setNombre("Tigre");

        List<AnimalResponseDto> lista = Arrays.asList(dto1, dto2);
        Mockito.when(animalService.obtenerTodosLosAnimales()).thenReturn(lista);

        mockMvc.perform(get("/api/animales")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("León"))
                .andExpect(jsonPath("$[1].nombre").value("Tigre"));
    }

    // ---------- POST create ----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void crearAnimal_RetornaCreated() throws Exception {
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("Elefante");

        doNothing().when(animalService).crearAnimal(any(AnimalRequestDto.class));

        mockMvc.perform(post("/api/animales")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.Mensaje").value("Animal creado correctamente."));
    }

    // ---------- PUT update ----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})
    void editarAnimal_RetornaOk() throws Exception {
        Long id = 1L;
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("León editado");

        doNothing().when(animalService).editarAnimal(eq(id), any(AnimalRequestDto.class));

        mockMvc.perform(put("/api/animales/{id}", id)
                        .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mensaje").value("Animal actualizado correctamente."));
    }

    // ---------- DELETE ----------
    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN"})  // o usar hasRole en el controller
    void eliminarAnimal_RetornaOk() throws Exception {
        Long id = 1L;
        doNothing().when(animalService).eliminarAnimal(id);

        mockMvc.perform(delete("/api/animales/{id}", id)
                        .with(csrf())) // 👈 agrega CSRF
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.Mensaje").value("Animal eliminado correctamente."));
    }

}