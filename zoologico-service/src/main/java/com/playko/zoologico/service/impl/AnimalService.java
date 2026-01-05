package com.playko.zoologico.service.impl;

import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
import com.playko.zoologico.dto.request.AnimalRequestDto;
import com.playko.zoologico.dto.response.AnimalRegistradoResponseDto;
import com.playko.zoologico.dto.response.AnimalResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.exception.ErrorGettingMailTokenException;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.animal.AnimalesNoEncontradosEnFechaException;
import com.playko.zoologico.exception.especie.EspecieNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IUsuarioRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.IAnimalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AnimalService implements IAnimalService {
    private final IAnimalRepository animalRepository;
    private final IEspecieRepository especieRepository;
    private final AuditorAware<Usuario> auditorProvider;

    @Override
    public AnimalResponseDto obtenerAnimalPorId(Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(AnimalNotFoundException::new);

        return mapToResponseDto(animal);
    }

    @Override
    public List<AnimalResponseDto> obtenerTodosLosAnimales() {
        List<Animal> animales = animalRepository.findAll();

        if (animales.isEmpty()) throw new NoDataFoundException();

        return animales.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public void crearAnimal(AnimalRequestDto dto) {
        Especie especie = especieRepository.findById(dto.getEspecieId())
                .orElseThrow(EspecieNotFoundException::new);


        Animal animal = new Animal();
        animal.setNombre(dto.getNombre().trim());
        animal.setFechaIngreso(dto.getFechaIngreso() != null ? dto.getFechaIngreso() : LocalDateTime.now());
        animal.setEspecie(especie);

        animalRepository.save(animal);
    }


    @Override
    public void editarAnimal(Long id, AnimalRequestDto dto) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(AnimalNotFoundException::new);

        Especie especie = especieRepository.findById(dto.getEspecieId())
                .orElseThrow(EspecieNotFoundException::new);

        animal.setNombre(dto.getNombre().trim());
        animal.setEspecie(especie);

        animalRepository.save(animal);
    }

    @Override
    public void eliminarAnimal(Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(AnimalNotFoundException::new);
        auditorProvider.getCurrentAuditor().ifPresent(animal::setDeletedBy);

        animalRepository.delete(animal);
    }

    @Override
    public List<AnimalRegistradoResponseDto> obtenerAnimalesRegistradosEnFecha(LocalDate fecha) {
        LocalDateTime inicioDelDia = fecha.atStartOfDay();
        LocalDateTime finDelDia = fecha.atTime(LocalTime.MAX);

        List<Animal> animales = animalRepository.findByFechaIngresoBetween(inicioDelDia, finDelDia);

        if (animales.isEmpty()) {
            throw new AnimalesNoEncontradosEnFechaException(fecha);
        }

        return animales.stream()
                .map(animal -> new AnimalRegistradoResponseDto(
                        animal.getNombre(),
                        animal.getEspecie().getNombre(),
                        animal.getEspecie().getZona().getNombre()
                ))
                .toList();
    }

    private AnimalResponseDto mapToResponseDto(Animal animal) {
        List<String> comentarios = animal.getComentarios() != null
                ? animal.getComentarios().stream().map(Comentario::getContenido).toList()
                : List.of();

        return new AnimalResponseDto(
                animal.getId(),
                animal.getNombre(),
                animal.getFechaIngreso(),
                animal.getEspecie().getId(),
                comentarios
        );
    }

    public String obtenerCorreoDelToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new ErrorGettingMailTokenException();
    }
}
