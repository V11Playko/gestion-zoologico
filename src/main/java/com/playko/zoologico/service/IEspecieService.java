package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.EspecieRequestDto;
import com.playko.zoologico.dto.response.AnimalesPorEspecieResponseDto;
import com.playko.zoologico.dto.response.EspecieResponseDto;

import java.util.List;

public interface IEspecieService {
    EspecieResponseDto obtenerEspeciePorId(Long id);
    List<EspecieResponseDto> obtenerTodasLasEspecies();
    void crearEspecie(EspecieRequestDto requestDto);
    void editarEspecie(Long id, EspecieRequestDto requestDto);
    void eliminarEspecie(Long id);

    List<AnimalesPorEspecieResponseDto> obtenerCantidadAnimalesPorEspecie();
}
