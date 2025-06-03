package com.playko.zoologico.service.impl;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.zona.ZonaAlreadyExistsException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.IZonaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ZonaService implements IZonaService {
    private final IZonaRepository zonaRepository;
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
        zonaRepository.delete(zona);
    }

    private ZonaResponseDto mapToResponseDto(Zona zona) {
        ZonaResponseDto dto = new ZonaResponseDto();
        dto.setId(zona.getId());
        dto.setNombre(zona.getNombre());
        List<String> nombresEspecies = zona.getEspecies()
                .stream()
                .map(Especie::getNombre)
                .toList();
        dto.setEspecies(nombresEspecies);
        return dto;
    }
}
