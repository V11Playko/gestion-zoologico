package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;

import java.util.List;

public interface IZonaService {
    ZonaResponseDto obtenerZonaPorId(Long id);

    List<ZonaResponseDto> obtenerTodasLasZonas();
    void crearZona(ZonaRequestDto requestDto);

    void editarZona(Long id, ZonaRequestDto requestDto);

    void eliminarZona(Long id);
}
