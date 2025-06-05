package com.playko.zoologico.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnimalRegistradoResponseDto {
    private String nombreAnimal;
    private String especie;
    private String zona;
}
