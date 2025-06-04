package com.playko.zoologico.service.impl;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.animal.ZonaConAnimalesException;
import com.playko.zoologico.exception.zona.ZonaAlreadyExistsException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.IZonaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
@RequiredArgsConstructor
public class ZonaService implements IZonaService {
    private final IZonaRepository zonaRepository;
    private final IAnimalRepository animalRepository;
    @Override
    public ZonaResponseDto obtenerZonaPorId(Long id) {
        Zona zona = zonaRepository.findById(id)
                .orElseThrow(ZonaNotFoundException::new);
        return mapToResponseDto(zona);
    }

    @Override
    public List<ZonaResponseDto> obtenerTodasLasZonas() {
        List<Zona> zonas = zonaRepository.findAll();

        if (zonas.isEmpty()) throw new NoDataFoundException();

        return zonas.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void crearZona(ZonaRequestDto requestDto) {
        if (zonaRepository.existsByNombreIgnoreCase(requestDto.getNombre())) {
            throw new ZonaAlreadyExistsException();
        }

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
            if (zonaRepository.existsByNombreIgnoreCase(nuevoNombre)) {
                throw new ZonaAlreadyExistsException();
            }
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

    private ZonaResponseDto mapToResponseDto(Zona zona) {
        List<String> nombresEspecies = zona.getEspecies() != null
                ? zona.getEspecies()
                .stream()
                .map(Especie::getNombre)
                .toList()
                : List.of();

        List<String> nombresAnimales = zona.getEspecies() != null
                ? zona.getEspecies()
                .stream()
                .flatMap(especie -> {
                    if (especie.getAnimales() == null) {
                        return Stream.<String>empty();
                    }
                    return especie.getAnimales()
                            .stream()
                            .map(Animal::getNombre);
                })
                .toList()
                : List.of();


        return new ZonaResponseDto(
                zona.getId(),
                zona.getNombre(),
                nombresEspecies,
                nombresAnimales
        );
    }
}
