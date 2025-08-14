package com.playko.zoologico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ComentarioExcelRowDto {
    private Long comentarioId;
    private String contenido;
    private String fecha; // formato ISO
    private Long autorId;
    private String autorNombre;
    private String autorEmail;
    private Long animalId;
    private String animalNombre;
    private Long padreId;
    private boolean esRespuesta;
    private Long creadorAnimalId;
    private String creadorAnimalNombre;
    private String creadorAnimalEmail;
}