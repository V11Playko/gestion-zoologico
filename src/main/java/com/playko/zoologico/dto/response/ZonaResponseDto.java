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
    private Long id;
    private String nombre;
    private List<String> especies;
    private List<String> nameAnimales;
}
