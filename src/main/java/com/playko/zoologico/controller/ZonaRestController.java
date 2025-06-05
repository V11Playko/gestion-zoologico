package com.playko.zoologico.controller;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.CantidadAnimalesPorZonaResponseDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.service.IZonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
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

import static com.playko.zoologico.constants.ExceptionMessages.NO_DATA_FOUND_MESSAGE;
import static com.playko.zoologico.constants.GlobalConstants.RESPONSE_MESSAGE_KEY;
import static com.playko.zoologico.constants.ZonaConstants.ZONA_CREATED_MESSAGE;
import static com.playko.zoologico.constants.ZonaConstants.ZONA_DELETED_MESSAGE;
import static com.playko.zoologico.constants.ZonaConstants.ZONA_UPDATED_MESSAGE;

@RestController
@RequestMapping("/api/zonas")
@RequiredArgsConstructor
public class ZonaRestController {

    private final IZonaService zonaService;

    @Operation(summary = "Obtener una zona por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zona encontrada"),
            @ApiResponse(responseCode = "404", description = "Zona no encontrada")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO')")
    public ResponseEntity<ZonaResponseDto> obtenerZonaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(zonaService.obtenerZonaPorId(id));
    }

    @Operation(summary = "Obtener todas las zonas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de zonas obtenido"),
            @ApiResponse(responseCode = "404", description = "No se encontraron zonas")
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO')")
    public ResponseEntity<List<ZonaResponseDto>> obtenerTodasLasZonas() {
        return ResponseEntity.ok(zonaService.obtenerTodasLasZonas());
    }

    @Operation(summary = "Crear una nueva zona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zona creada correctamente"),
            @ApiResponse(responseCode = "409", description = "Ya existe una zona con ese nombre")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> crearZona(@Valid @RequestBody ZonaRequestDto requestDto) {
        zonaService.crearZona(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ZONA_CREATED_MESSAGE));
    }

    @Operation(summary = "Editar una zona existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zona editada correctamente"),
            @ApiResponse(responseCode = "404", description = "Zona no encontrada"),
            @ApiResponse(responseCode = "409", description = "Nombre de zona ya existe")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> editarZona(@PathVariable Long id, @Valid @RequestBody ZonaRequestDto requestDto) {
        zonaService.editarZona(id, requestDto);
        return ResponseEntity.ok(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ZONA_UPDATED_MESSAGE));
    }

    @Operation(summary = "Eliminar una zona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zona eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Zona no encontrada")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarZona(@PathVariable Long id) {
        zonaService.eliminarZona(id);
        return ResponseEntity.ok(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ZONA_DELETED_MESSAGE));
    }

    @Operation(summary = "Obtener la cantidad de animales por zona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cantidad de animales por zona obtenida", content = @Content),
            @ApiResponse(responseCode = "404", description = NO_DATA_FOUND_MESSAGE, content = @Content)
    })
    @GetMapping("/indicador/cantidadAnimalesPorZona")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<CantidadAnimalesPorZonaResponseDto>> obtenerCantidadAnimalesPorZona() {
        return ResponseEntity.ok(zonaService.obtenerCantidadAnimalesPorZona());
    }
}
