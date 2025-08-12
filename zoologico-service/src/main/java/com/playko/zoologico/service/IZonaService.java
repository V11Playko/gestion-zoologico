package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.CantidadAnimalesPorZonaResponseDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IZonaService {
    ZonaResponseDto obtenerZonaPorId(Long id);
    Page<ZonaResponseDto> obtenerTodasLasZonas(String nombre, int page);
    void crearZona(ZonaRequestDto requestDto);
    void editarZona(Long id, ZonaRequestDto requestDto);
    void eliminarZona(Long id);
    List<CantidadAnimalesPorZonaResponseDto> obtenerCantidadAnimalesPorZona();
}
