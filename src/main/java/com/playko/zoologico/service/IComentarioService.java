package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.ComentarioRequestDto;
import com.playko.zoologico.dto.response.ComentarioResponseDto;
import com.playko.zoologico.dto.response.PorcentajeComentariosConRespuestasDto;

import java.util.List;

public interface IComentarioService {
    void agregarComentario(ComentarioRequestDto dto);
    List<ComentarioResponseDto> obtenerMuroDeAnimal(String animalName);
    PorcentajeComentariosConRespuestasDto obtenerPorcentajeComentariosConRespuestas();
}
