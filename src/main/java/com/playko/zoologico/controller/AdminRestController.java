package com.playko.zoologico.controller;

import com.playko.zoologico.configuration.Constants;
import com.playko.zoologico.dto.request.EspecieRequestDto;
import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.EspecieResponseDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.service.IEspecieService;
import com.playko.zoologico.service.IZonaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/v1/admin")
@RequiredArgsConstructor
public class AdminRestController {

    private final IZonaService zonaService;
    private final IEspecieService especieService;

    /**
     *
     * Endpoints para ZONA
     *
     */

    @Operation(summary = "Obtener una zona por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zona encontrada", content = @Content),
            @ApiResponse(responseCode = "404", description = Constants.ZONA_NOT_FOUND_MESSAGE, content = @Content)
    })
    @GetMapping("/zona/{id}")
    public ResponseEntity<ZonaResponseDto> obtenerZonaPorId(@PathVariable Long id) {
        return ResponseEntity.ok(zonaService.obtenerZonaPorId(id));
    }

    @Operation(summary = "Obtener todas las zonas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de zonas obtenido", content = @Content),
            @ApiResponse(responseCode = "404", description = Constants.NO_DATA_FOUND_MESSAGE, content = @Content)
    })
    @GetMapping("/zonas")
    public ResponseEntity<List<ZonaResponseDto>> obtenerTodasLasZonas() {
        return ResponseEntity.ok(zonaService.obtenerTodasLasZonas());
    }

    @Operation(summary = "Crear una nueva zona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Zona creada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Map"))),
            @ApiResponse(responseCode = "409", description = Constants.ZONA_ALREADY_EXISTS,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error")))
    })
    @PostMapping("/zona")
    public ResponseEntity<Map<String, String>> crearZona(@Valid @RequestBody ZonaRequestDto requestDto) {
        zonaService.crearZona(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ZONA_CREATED_MESSAGE));
    }

    @Operation(summary = "Editar una zona existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zona editada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Map"))),
            @ApiResponse(responseCode = "404", description = Constants.ZONA_NOT_FOUND_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error"))),
            @ApiResponse(responseCode = "409", description = Constants.ZONA_ALREADY_EXISTS,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error")))
    })
    @PutMapping("/zona/{id}")
    public ResponseEntity<Map<String, String>> editarZona(@PathVariable Long id, @Valid @RequestBody ZonaRequestDto requestDto) {
        zonaService.editarZona(id, requestDto);
        return ResponseEntity.ok(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ZONA_UPDATED_MESSAGE));
    }

    @Operation(summary = "Eliminar una zona")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Zona eliminada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Map"))),
            @ApiResponse(responseCode = "404", description = Constants.ZONA_NOT_FOUND_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error")))
    })
    @DeleteMapping("/zona/{id}")
    public ResponseEntity<Map<String, String>> eliminarZona(@PathVariable Long id) {
        zonaService.eliminarZona(id);
        return ResponseEntity.ok(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ZONA_DELETED_MESSAGE));
    }


    /**
     *
     * Endpoints para ESPECIE
     *
     */

    @Operation(summary = "Obtener una especie por su ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especie encontrada", content = @Content),
            @ApiResponse(responseCode = "404", description = Constants.ESPECIE_NOT_FOUND_MESSAGE, content = @Content)
    })
    @GetMapping("/especie/{id}")
    public ResponseEntity<EspecieResponseDto> obtenerEspeciePorId(@PathVariable Long id) {
        return ResponseEntity.ok(especieService.obtenerEspeciePorId(id));
    }

    @Operation(summary = "Obtener todas las especies")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de especies obtenido", content = @Content),
            @ApiResponse(responseCode = "404", description = Constants.NO_DATA_FOUND_MESSAGE, content = @Content)
    })
    @GetMapping("/especies")
    public ResponseEntity<List<EspecieResponseDto>> obtenerTodasLasEspecies() {
        return ResponseEntity.ok(especieService.obtenerTodasLasEspecies());
    }

    @Operation(summary = "Crear una nueva especie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Especie creada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Map"))),
            @ApiResponse(responseCode = "409", description = Constants.ESPECIE_ALREADY_EXISTS_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error"))),
            @ApiResponse(responseCode = "404", description = Constants.ZONA_NOT_FOUND_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error")))
    })
    @PostMapping("/especie")
    public ResponseEntity<Map<String, String>> crearEspecie(@Valid @RequestBody EspecieRequestDto dto) {
        especieService.crearEspecie(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ESPECIE_CREATED_MESSAGE));
    }

    @Operation(summary = "Editar una especie existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especie editada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Map"))),
            @ApiResponse(responseCode = "404", description = Constants.ESPECIE_NOT_FOUND_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error"))),
            @ApiResponse(responseCode = "409", description = Constants.ESPECIE_ALREADY_EXISTS_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error")))
    })
    @PutMapping("/especie/{id}")
    public ResponseEntity<Map<String, String>> editarEspecie(@PathVariable Long id, @Valid @RequestBody EspecieRequestDto dto) {
        especieService.editarEspecie(id, dto);
        return ResponseEntity.ok(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ESPECIE_UPDATED_MESSAGE));
    }

    @Operation(summary = "Eliminar una especie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Especie eliminada correctamente",
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Map"))),
            @ApiResponse(responseCode = "404", description = Constants.ESPECIE_NOT_FOUND_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error"))),
            @ApiResponse(responseCode = "409", description = Constants.ESPECIE_CON_ANIMALES_MESSAGE,
                    content = @Content(mediaType = "application/json", schema = @Schema(ref = "#/components/schemas/Error")))
    })
    @DeleteMapping("/especie/{id}")
    public ResponseEntity<Map<String, String>> eliminarEspecie(@PathVariable Long id) {
        especieService.eliminarEspecie(id);
        return ResponseEntity.ok(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.ESPECIE_DELETED_MESSAGE));
    }
}
