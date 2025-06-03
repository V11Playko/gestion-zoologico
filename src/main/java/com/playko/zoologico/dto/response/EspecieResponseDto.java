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
public class EspecieResponseDto {
    private Long id;
    private String nombre;
    private String zonaName;
    private List<String> nameAnimales;
}
