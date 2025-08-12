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
public class ZonaResponseDto {
    private String nombre;
    private List<IdAndEspecieResponseDto> especies;
    private Long cantidadAnimales;
}
