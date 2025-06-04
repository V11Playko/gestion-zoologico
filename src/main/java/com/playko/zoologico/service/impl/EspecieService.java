package com.playko.zoologico.service.impl;

import com.playko.zoologico.dto.request.EspecieRequestDto;
import com.playko.zoologico.dto.response.EspecieResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.especie.EspecieAlreadyExistsException;
import com.playko.zoologico.exception.especie.EspecieConAnimalesException;
import com.playko.zoologico.exception.especie.EspecieNotFoundException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.IEspecieService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class EspecieService implements IEspecieService {
    private final IEspecieRepository especieRepository;
    private final IZonaRepository zonaRepository;
    private final IAnimalRepository animalRepository;

    @Override
    public EspecieResponseDto obtenerEspeciePorId(Long id) {
        Especie especie = especieRepository.findById(id)
                .orElseThrow(EspecieNotFoundException::new);

        return mapToResponseDto(especie);
    }

    @Override
    public List<EspecieResponseDto> obtenerTodasLasEspecies() {
        List<Especie> especies = especieRepository.findAll();

        if (especies.isEmpty()) throw new NoDataFoundException();

        return especies.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public void crearEspecie(EspecieRequestDto dto) {
        if (especieRepository.existsByNombreIgnoreCase(dto.getNombre())) {
            throw new EspecieAlreadyExistsException();
        }

        Zona zona = zonaRepository.findByNombreIgnoreCase(dto.getZonaName())
                .orElseThrow(ZonaNotFoundException::new);

        Especie especie = new Especie();
        especie.setNombre(dto.getNombre().trim());
        especie.setZona(zona);

        especieRepository.save(especie);
    }

    @Override
    public void editarEspecie(Long id, EspecieRequestDto dto) {
        Especie especie = especieRepository.findById(id)
                .orElseThrow(EspecieNotFoundException::new);

        String nuevoNombre = dto.getNombre().trim();
        if (!especie.getNombre().equalsIgnoreCase(nuevoNombre)
                && especieRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new EspecieAlreadyExistsException();
        }

        Zona zona = zonaRepository.findByNombreIgnoreCase(dto.getZonaName())
                .orElseThrow(ZonaNotFoundException::new);

        especie.setNombre(nuevoNombre);
        especie.setZona(zona);

        especieRepository.save(especie);
    }

    @Override
    public void eliminarEspecie(Long id) {
        Especie especie = especieRepository.findById(id)
                .orElseThrow(EspecieNotFoundException::new);

        boolean tieneAnimales = animalRepository.existsByEspecie(especie);
        if (tieneAnimales) {
            throw new EspecieConAnimalesException();
        }

        especieRepository.delete(especie);
    }

    private EspecieResponseDto mapToResponseDto(Especie especie) {
        List<String> nombresAnimales = especie.getAnimales() != null ?
                especie.getAnimales().stream()
                        .map(Animal::getNombre)
                        .collect(Collectors.toList())
                : List.of();

        return new EspecieResponseDto(
                especie.getId(),
                especie.getNombre(),
                especie.getZona().getNombre(),
                nombresAnimales
        );
    }
}
