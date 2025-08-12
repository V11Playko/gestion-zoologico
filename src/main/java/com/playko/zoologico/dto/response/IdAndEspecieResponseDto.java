package com.playko.zoologico.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class IdAndEspecieResponseDto {
    private Long id;
    private String nombre;
}
