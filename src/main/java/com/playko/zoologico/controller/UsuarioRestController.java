package com.playko.zoologico.controller;

import com.playko.zoologico.configuration.Constants;
import com.playko.zoologico.dto.request.UsuarioRequestDto;
import com.playko.zoologico.dto.response.UsuarioResponseDto;
import com.playko.zoologico.service.IUsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioRestController {

    private final IUsuarioService usuarioService;

    @Operation(summary = "Listar todos los usuarios")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Listado de usuarios obtenido"),
            @ApiResponse(responseCode = "404", description = "No se encontraron usuarios")
    })
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<UsuarioResponseDto>> listarUsuarios() {
        List<UsuarioResponseDto> usuarios = usuarioService.listarUsuarios();
        return ResponseEntity.ok(usuarios);
    }

    @Operation(summary = "Crear un nuevo usuario empleado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Usuario empleado creado correctamente"),
            @ApiResponse(responseCode = "409", description = "El correo ya existe"),
            @ApiResponse(responseCode = "404", description = "Rol no encontrado")
    })
    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Map<String, String>> crearUsuarioEmpleado(@Valid @RequestBody UsuarioRequestDto dto) {
        usuarioService.crearUsuarioEmpleado(dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Collections.singletonMap(Constants.RESPONSE_MESSAGE_KEY, Constants.USUARIO_CREATED_MESSAGE));
    }
}
