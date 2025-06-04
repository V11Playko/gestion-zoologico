package com.playko.zoologico.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EditarEspecieRequestDto {
    private String nombre;
    private String zonaName;
}
