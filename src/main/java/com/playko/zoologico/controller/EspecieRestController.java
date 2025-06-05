package com.playko.zoologico.controller;


import com.playko.zoologico.configuration.Constants;
import com.playko.zoologico.dto.request.EspecieRequestDto;
import com.playko.zoologico.dto.response.AnimalesPorEspecieResponseDto;
import com.playko.zoologico.dto.response.EspecieResponseDto;
import com.playko.zoologico.service.IEspecieService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/especies")
@RequiredArgsConstructor
public class EspecieRestController {

    private final IEspecieService especieService;

    @Operation(summary = "Obtener una especie por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especie encontrada"),
            @ApiResponse(responseCode = "404", description = "Especie no encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO')")
    public ResponseEntity<EspecieResponseDto> obtenerEspeciePorId(@PathVariable Long id) {
        return ResponseEntity.ok(especieService.obtenerEspeciePorId(id));
    }

    @Operation(summary = "Obtener todas las especies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de especies obtenido"),
            @ApiResponse(responseCode = "404", description = "No se encontraron especies")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO')")
    public ResponseEntity<List<EspecieResponseDto>> obtenerTodasLasEspecies() {
        return ResponseEntity.ok(especieService.obtenerTodasLasEspecies());
    }

    @Operation(summary = "Crear una nueva especie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Especie creada correctamente"),
            @ApiResponse(responseCode = "409", description = "Ya existe una especie con ese nombre"),
            @ApiResponse(responseCode = "404", description = "Zona no encontrada para asignar")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> crearEspecie(@Valid @RequestBody EspecieRequestDto dto) {
        especieService.crearEspecie(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ESPECIE_CREATED_MESSAGE));
    }

    @Operation(summary = "Editar una especie existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especie editada correctamente"),
            @ApiResponse(responseCode = "404", description = "Especie no encontrada"),
            @ApiResponse(responseCode = "409", description = "Nombre de especie ya existe")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> editarEspecie(@PathVariable Long id, @Valid @RequestBody EspecieRequestDto dto) {
        especieService.editarEspecie(id, dto);
        return ResponseEntity.ok(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ESPECIE_UPDATED_MESSAGE));
    }

    @Operation(summary = "Eliminar una especie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especie eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Especie no encontrada"),
            @ApiResponse(responseCode = "409", description = "La especie tiene animales asociados")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarEspecie(@PathVariable Long id) {
        especieService.eliminarEspecie(id);
        return ResponseEntity.ok(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ESPECIE_DELETED_MESSAGE));
    }

    @Operation(summary = "Obtener la cantidad de animales por especie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad de animales por especie obtenida correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnimalesPorEspecieResponseDto.class))),
            @ApiResponse(responseCode = "404", description = Constants.NO_DATA_FOUND_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error")))
    })
    @GetMapping("/indicador/animalesPorEspecie")
    public ResponseEntity<List<AnimalesPorEspecieResponseDto>> obtenerCantidadAnimalesPorEspecie() {
        return ResponseEntity.ok(especieService.obtenerCantidadAnimalesPorEspecie());
    }

}
