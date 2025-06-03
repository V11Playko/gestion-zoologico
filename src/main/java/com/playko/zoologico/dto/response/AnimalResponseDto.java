package com.playko.zoologico.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnimalResponseDto {
    private Long id;
    private String nombre;
    private LocalDateTime fechaIngreso;
    private String especieName;
    private List<String> comentarios;
}
