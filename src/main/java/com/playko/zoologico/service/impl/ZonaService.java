package com.playko.zoologico.service.impl;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.CantidadAnimalesPorZonaResponseDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.animal.ZonaConAnimalesException;
import com.playko.zoologico.exception.zona.ZonaAlreadyExistsException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.IZonaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class ZonaService implements IZonaService {
    private final IZonaRepository zonaRepository;
    private final IAnimalRepository animalRepository;
    private final IEspecieRepository especieRepository;
    @Override
    public ZonaResponseDto obtenerZonaPorId(Long id) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(ZonaNotFoundException::new);
        return mapToResponseDto(zona);
    }

    @Override
    public List<ZonaResponseDto> obtenerTodasLasZonas() {
        // Esta consulta ya hace un join fetch de especies y animales
        List<Zona> zonas = zonaRepository.findAllWithEspeciesAndAnimales();
        if (zonas.isEmpty()) {
            throw new NoDataFoundException();
        }

        return zonas.stream()
                .map(zona -> {
                    List<Long> idsEspecies = zona.getEspecies().stream()
                            .map(Especie::getId)
                            .toList();

                    List<Long> idsAnimales = zona.getEspecies().stream()
                            .flatMap(especie -> especie.getAnimales().stream())
                            .map(Animal::getId)
                            .toList();

                    return new ZonaResponseDto(
                            zona.getId(),
                            zona.getNombre(),
                            idsEspecies,
                            idsAnimales
                    );
                })
                .toList();
    }


    @Override
    public void crearZona(ZonaRequestDto requestDto) {
        Zona nuevaZona = new Zona();
        nuevaZona.setNombre(requestDto.getNombre().trim());
        zonaRepository.save(nuevaZona);
    }

    @Override
    public void editarZona(Long id, ZonaRequestDto requestDto) {
        Zona zonaExistente = zonaRepository.findById(id)
                .orElseThrow(ZonaNotFoundException::new);

        String nuevoNombre = requestDto.getNombre().trim();

        if (!zonaExistente.getNombre().equalsIgnoreCase(nuevoNombre)) {
            zonaExistente.setNombre(nuevoNombre);
        }
        zonaRepository.save(zonaExistente);
    }

    @Override
    public void eliminarZona(Long id) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(ZonaNotFoundException::new);

        boolean hayAnimales = animalRepository.existsByEspecie_Zona(zona);
        if (hayAnimales) {
            throw new ZonaConAnimalesException();
        }

        zonaRepository.delete(zona);
    }

    @Override
    public List<CantidadAnimalesPorZonaResponseDto> obtenerCantidadAnimalesPorZona() {
        List<Zona> zonas = zonaRepository.findAll();

        if (zonas.isEmpty()) {
            throw new NoDataFoundException();
        }

        return zonas.stream().map(zona -> {
            long cantidadAnimales = zona.getEspecies() != null
                    ? zona.getEspecies().stream()
                    .filter(especie -> especie.getAnimales() != null)
                    .mapToLong(especie -> especie.getAnimales().size())
                    .sum()
                    : 0L;

            return new CantidadAnimalesPorZonaResponseDto(zona.getNombre(), cantidadAnimales);
        }).collect(Collectors.toList());
    }

    private ZonaResponseDto mapToResponseDto(Zona zona) {
        List<Long> idsEspecies = zona.getEspecies() != null
                ? zona.getEspecies().stream()
                .map(Especie::getId)
                .toList()
                : List.of();

        List<Long> idsAnimales = zona.getEspecies() != null
                ? zona.getEspecies().stream()
                .flatMap(especie -> especie.getAnimales() != null
                        ? especie.getAnimales().stream().map(Animal::getId)
                        : Stream.<Long>empty())
                .toList()
                : List.of();

        return new ZonaResponseDto(
                zona.getId(),
                zona.getNombre(),
                idsEspecies,
                idsAnimales
        );
    }

}
