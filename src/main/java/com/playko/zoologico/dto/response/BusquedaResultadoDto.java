package com.playko.zoologico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BusquedaResultadoDto {
    private String tipoResultado;
    private String zonaNombre;
    private String especieNombre;
    private String animalNombre;
    private String comentarioContenido;
    private String respuestaContenido;
}
