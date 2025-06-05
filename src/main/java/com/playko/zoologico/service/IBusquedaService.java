package com.playko.zoologico.service;

import com.playko.zoologico.dto.response.BusquedaResultadoDto;

import java.util.List;

public interface IBusquedaService {
    List<BusquedaResultadoDto> buscarPorPalabra(String palabra);
}
