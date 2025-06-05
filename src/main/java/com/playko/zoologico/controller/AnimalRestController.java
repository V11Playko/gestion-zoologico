package com.playko.zoologico.controller;

import com.playko.zoologico.configuration.Constants;
import com.playko.zoologico.dto.request.AnimalRequestDto;
import com.playko.zoologico.dto.response.AnimalRegistradoResponseDto;
import com.playko.zoologico.dto.response.AnimalResponseDto;
import com.playko.zoologico.exception.animal.FechaFormatoInvalidoException;
import com.playko.zoologico.service.IAnimalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/animales")
@RequiredArgsConstructor
public class AnimalRestController {

    private final IAnimalService animalService;

    @Operation(summary = "Obtener un animal por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal encontrado"),
            @ApiResponse(responseCode = "404", description = "Animal no encontrado")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO')")
    public ResponseEntity<AnimalResponseDto> obtenerAnimalPorId(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.obtenerAnimalPorId(id));
    }

    @Operation(summary = "Obtener todos los animales")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de animales obtenido"),
            @ApiResponse(responseCode = "404", description = "No se encontraron animales")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO')")
    public ResponseEntity<List<AnimalResponseDto>> obtenerTodosLosAnimales() {
        List<AnimalResponseDto> animales = animalService.obtenerTodosLosAnimales();
        return ResponseEntity.ok(animales);
    }

    @Operation(summary = "Crear un nuevo animal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Animal creado correctamente"),
            @ApiResponse(responseCode = "404", description = "Especie no encontrada")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> crearAnimal(@Valid @RequestBody AnimalRequestDto dto) {
        animalService.crearAnimal(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ANIMAL_CREATED_MESSAGE));
    }

    @Operation(summary = "Editar un animal existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal editado correctamente"),
            @ApiResponse(responseCode = "404", description = "Animal o especie no encontrada")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> editarAnimal(@PathVariable Long id, @Valid @RequestBody AnimalRequestDto dto) {
        animalService.editarAnimal(id, dto);
        return ResponseEntity.ok(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ANIMAL_UPDATED_MESSAGE));
    }

    @Operation(summary = "Eliminar un animal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animal eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Animal no encontrado")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarAnimal(@PathVariable Long id) {
        animalService.eliminarAnimal(id);
        return ResponseEntity.ok(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ANIMAL_DELETED_MESSAGE));
    }

    @Operation(summary = "Obtener animales registrados en una fecha específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Animales registrados obtenidos correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnimalRegistradoResponseDto.class))),
            @ApiResponse(responseCode = "404", description = Constants.NO_DATA_FOUND_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error")))
    })
    @GetMapping("/indicador/animalesRegistradosEnFecha")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<AnimalRegistradoResponseDto> obtenerAnimalesRegistrados(@RequestParam String fecha) {
        try {
            LocalDate fechaParseada = LocalDate.parse(fecha, DateTimeFormatter.ISO_LOCAL_DATE);
            return animalService.obtenerAnimalesRegistradosEnFecha(fechaParseada);
        } catch (DateTimeParseException e) {
            throw new FechaFormatoInvalidoException(fecha);
        }
    }

}
