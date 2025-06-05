package com.playko.zoologico.controller;

import com.playko.zoologico.dto.response.BusquedaResultadoDto;
import com.playko.zoologico.service.IBusquedaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/busqueda")
@RequiredArgsConstructor
public class BusquedaRestController {
    private final IBusquedaService busquedaService;


    @Operation(summary = "Buscar coincidencias por palabra clave")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda realizada exitosamente",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = BusquedaResultadoDto.class)))),
            @ApiResponse(responseCode = "204", description = "Sin resultados encontrados")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<BusquedaResultadoDto>> buscarPorPalabra(@RequestParam String palabra) {
        List<BusquedaResultadoDto> resultados = busquedaService.buscarPorPalabra(palabra);

        if (resultados.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(resultados);
    }
}
