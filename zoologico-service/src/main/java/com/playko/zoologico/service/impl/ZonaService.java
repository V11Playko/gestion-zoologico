package com.playko.zoologico.service.impl;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.CantidadAnimalesPorZonaResponseDto;
import com.playko.zoologico.dto.response.IdAndEspecieResponseDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.NonNegativePageNumberException;
import com.playko.zoologico.exception.animal.ZonaConAnimalesException;
import com.playko.zoologico.exception.zona.IdZonaInvalidException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.IZonaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZonaService implements IZonaService {
    private final IZonaRepository zonaRepository;
    private final IAnimalRepository animalRepository;
    @Override
    public ZonaResponseDto obtenerZonaPorId(Long id) {
        if (id == null || id <= 0) {
            throw new IdZonaInvalidException();
        }

        Zona zona = zonaRepository.findById(id)
                .orElseThrow(ZonaNotFoundException::new);
        return mapToResponseDto(zona);
    }

    @Override
    public Page<ZonaResponseDto> obtenerTodasLasZonas(String nombre, int page) {
        if (page<0) throw new NonNegativePageNumberException();

        Pageable pageable = PageRequest.of(page, 5);

        Page<Zona> zonasPage = zonaRepository.findZonasPaginadas(nombre, pageable);

        if (zonasPage.isEmpty()) {
            throw new NoDataFoundException();
        }

        List<Long> ids = zonasPage.getContent().stream().map(Zona::getId).toList();
        List<Zona> zonasConFetch = zonaRepository.findZonasWithFetchByIds(ids);

        Map<Long, Zona> zonasMap = zonasConFetch.stream()
                .collect(Collectors.toMap(Zona::getId, z -> z));

        List<ZonaResponseDto> dtoList = zonasPage.getContent().stream()
                .map(z -> mapToResponseDto(zonasMap.get(z.getId())))
                .toList();

        return new PageImpl<>(dtoList, pageable, zonasPage.getTotalElements());
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
        }).toList();
    }

    private ZonaResponseDto mapToResponseDto(Zona zona) {
        Set<Especie> especies = zona.getEspecies();

        List<IdAndEspecieResponseDto> especiesDto =
                especies != null
                        ? especies.stream()
                        .map(e -> new IdAndEspecieResponseDto(e.getId(), e.getNombre()))
                        .toList()
                        : Collections.emptyList();

        long cantidadAnimales =
                especies != null
                        ? especies.stream()
                        .flatMap(e -> Optional.ofNullable(e.getAnimales()).orElse(Collections.emptySet()).stream())
                        .count()
                        : 0;

        return new ZonaResponseDto(
                zona.getNombre(),
                especiesDto,
                cantidadAnimales
        );
    }

}
