package com.playko.zoologico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CantidadAnimalesPorZonaResponseDto {
    private String nombreZona;
    private Long cantidadAnimales;
}
