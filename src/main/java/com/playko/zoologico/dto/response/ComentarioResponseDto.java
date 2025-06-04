package com.playko.zoologico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioResponseDto {
    private Long id;
    private String contenido;
    private String fecha;
    private String autorNombre;
    private List<ComentarioResponseDto> respuestas;
}
