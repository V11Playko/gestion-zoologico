package com.playko.zoologico.controller;

import com.playko.zoologico.configuration.Constants;
import com.playko.zoologico.dto.request.ComentarioRequestDto;
import com.playko.zoologico.dto.response.ComentarioResponseDto;
import com.playko.zoologico.service.IComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comentarios")
@RequiredArgsConstructor
public class ComentarioRestController {

    private final IComentarioService comentarioService;

    @Operation(summary = "Agregar un nuevo comentario a un animal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Comentario agregado correctamente"),
            @ApiResponse(responseCode = "404", description = "Animal o comentario padre no encontrado")
    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO')")
    public ResponseEntity<Map<String, String>> agregarComentario(@Valid @RequestBody ComentarioRequestDto dto) {
        comentarioService.agregarComentario(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.COMENTARIO_AGREGADO_MESSAGE));
    }

    @Operation(summary = "Obtener muro de comentarios de un animal")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Muro de comentarios obtenido"),
            @ApiResponse(responseCode = "404", description = "Animal no encontrado")
    })
    @GetMapping("/muro/{animalNombre}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_EMPLEADO')")
    public ResponseEntity<List<ComentarioResponseDto>> obtenerMuroDeAnimal(@PathVariable String animalNombre) {
        return ResponseEntity.ok(comentarioService.obtenerMuroDeAnimal(animalNombre));
    }
}
