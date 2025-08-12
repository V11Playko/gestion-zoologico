package com.playko.zoologico.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AnimalRequestDto {
    @NotBlank(message = "El nombre del animal es obligatorio")
    private String nombre;
    private Long especieId;
    private LocalDateTime fechaIngreso;
}
