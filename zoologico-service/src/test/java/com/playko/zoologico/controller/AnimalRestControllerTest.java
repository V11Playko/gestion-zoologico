package com.playko.zoologico.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.zoologico.dto.request.AnimalRequestDto;
import com.playko.zoologico.dto.response.AnimalResponseDto;
import com.playko.zoologico.service.IAnimalService;
import org.apache.catalina.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AnimalRestController.class)
@Import(SecurityConfig.class) // importa tu config de seguridad
class AnimalRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; // para convertir DTOs a JSON

    @MockBean
    private IAnimalService animalService;

    // --- GET /api/animales/{id} ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void obtenerAnimalPorId_ShouldReturnAnimal_WhenExists() throws Exception {
        AnimalResponseDto dto = new AnimalResponseDto();
        dto.setId(1L);
        dto.setNombre("Juaco");
        when(animalService.obtenerAnimalPorId(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/animales/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Juaco")));
    }

    // --- POST /api/animales ---
    @Test
    @WithMockUser(roles = "ADMIN")
    void crearAnimal_ShouldReturnCreated_WhenValidDto() throws Exception {
        AnimalRequestDto dto = new AnimalRequestDto();
        dto.setNombre("Pez");

        doNothing().when(animalService).crearAnimal(dto);

        mockMvc.perform(post("/api/animales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.Mensaje", is("Animal creado correctamente.")));
    }

    // --- GET /api/animales ---
    @Test
    @WithMockUser(roles = "EMPLEADO")
    void obtenerTodosLosAnimales_ShouldReturnList() throws Exception {
        AnimalResponseDto dto = new AnimalResponseDto();
        dto.setId(1L);
        dto.setNombre("León");

        when(animalService.obtenerTodosLosAnimales()).thenReturn(List.of(dto));

        mockMvc.perform(get("/api/animales"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre", is("León")));
    }

    // --- Seguridad: acceso denegado ---
    @Test
    @WithMockUser(roles = "CLIENTE")
    void crearAnimal_ConRolCliente_DeberiaRetornar403() throws Exception {
        AnimalRequestDto dto = new AnimalRequestDto();
        dto.setNombre("Tigre");

        mockMvc.perform(post("/api/animales")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }
}