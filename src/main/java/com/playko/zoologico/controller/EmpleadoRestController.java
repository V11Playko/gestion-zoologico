package com.playko.zoologico.controller;

import com.playko.zoologico.configuration.Constants;
import com.playko.zoologico.dto.response.AnimalResponseDto;
import com.playko.zoologico.dto.response.EspecieResponseDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.service.IAnimalService;
import com.playko.zoologico.service.IEspecieService;
import com.playko.zoologico.service.IZonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/v1/empleado")
@RequiredArgsConstructor
public class EmpleadoRestController {
    private final IZonaService zonaService;
    private final IEspecieService especieService;
    private final IAnimalService animalService;

    /**
     * Endpoints para ZONAS
     */
    @Operation(summary = "Obtener todas las zonas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de zonas obtenido", content = @Content),
            @ApiResponse(responseCode = "404", description = Constants.NO_DATA_FOUND_MESSAGE, content = @Content)
    })
    @GetMapping("/zonas")
    public ResponseEntity<List<ZonaResponseDto>> obtenerTodasLasZonas() {
        return ResponseEntity.ok(zonaService.obtenerTodasLasZonas());
    }


    /**
     * Endpoints para ESPECIES
     */
    @Operation(summary = "Obtener todas las especies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de especies obtenido", content = @Content),
            @ApiResponse(responseCode = "404", description = Constants.NO_DATA_FOUND_MESSAGE, content = @Content)
    })
    @GetMapping("/especies")
    public ResponseEntity<List<EspecieResponseDto>> obtenerTodasLasEspecies() {
        return ResponseEntity.ok(especieService.obtenerTodasLasEspecies());
    }

    /**
     * Endpoints para ANIMAL
     */
    @Operation(summary = "Obtener todos los animales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de animales obtenido", content = @Content),
            @ApiResponse(responseCode = "404", description = Constants.NO_DATA_FOUND_MESSAGE, content = @Content)
    })
    @GetMapping("/animales")
    public ResponseEntity<List<AnimalResponseDto>> obtenerTodosLosAnimales() {
        List<AnimalResponseDto> animales = animalService.obtenerTodosLosAnimales();
        return ResponseEntity.ok(animales);
    }
}
