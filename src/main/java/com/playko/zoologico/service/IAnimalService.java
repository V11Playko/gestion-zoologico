package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.AnimalRequestDto;
import com.playko.zoologico.dto.response.AnimalRegistradoResponseDto;
import com.playko.zoologico.dto.response.AnimalResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface IAnimalService {
    AnimalResponseDto obtenerAnimalPorId(Long id);
    List<AnimalResponseDto> obtenerTodosLosAnimales();
    void crearAnimal(AnimalRequestDto requestDto);
    void editarAnimal(Long id, AnimalRequestDto requestDto);
    void eliminarAnimal(Long id);

    List<AnimalRegistradoResponseDto> obtenerAnimalesRegistradosEnFecha(LocalDate fecha);
}
