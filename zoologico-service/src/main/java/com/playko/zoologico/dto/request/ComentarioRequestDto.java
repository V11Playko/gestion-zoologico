package com.playko.zoologico.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioRequestDto {
    private String contenido;
    private Long animalId;
    private Long autorId;
    private Long padreId;
}
