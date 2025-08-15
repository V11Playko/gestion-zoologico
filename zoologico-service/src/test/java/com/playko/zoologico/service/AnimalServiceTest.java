package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.AnimalRequestDto;
import com.playko.zoologico.dto.response.AnimalRegistradoResponseDto;
import com.playko.zoologico.dto.response.AnimalResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.animal.AnimalesNoEncontradosEnFechaException;
import com.playko.zoologico.exception.especie.EspecieNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.impl.AnimalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private IAnimalRepository animalRepository;

    @Mock
    private IEspecieRepository especieRepository;

    @Mock
    private IZonaRepository zonaRepository;

    @InjectMocks
    private AnimalService animalService;

    private Animal animal;
    private Especie especie;
    private AnimalRequestDto requestDto;

    @BeforeEach
    void setUp() {
        especie = new Especie();
        especie.setId(1L);
        especie.setNombre("León");
        especie.setZona(new Zona(1L, "Zona Safari", null));

        animal = new Animal();
        animal.setId(1L);
        animal.setNombre("Simba");
        animal.setFechaIngreso(LocalDateTime.of(2025, 1, 1, 10, 0));
        animal.setEspecie(especie);
        animal.setComentarios(List.of());

        requestDto = new AnimalRequestDto("Simba", 1L, LocalDateTime.of(2025, 1, 1, 10, 0));
    }

    @Test
    void obtenerAnimalPorId_exitoso() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        AnimalResponseDto result = animalService.obtenerAnimalPorId(1L);

        assertEquals("Simba", result.getNombre());
        assertEquals(1L, result.getEspecieId());
        verify(animalRepository).findById(1L);
    }

    @Test
    void obtenerAnimalPorId_noEncontrado_lanzaExcepcion() {
        when(animalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class, () -> animalService.obtenerAnimalPorId(1L));
    }

    @Test
    void obtenerTodosLosAnimales_exitoso() {
        when(animalRepository.findAll()).thenReturn(List.of(animal));

        List<AnimalResponseDto> result = animalService.obtenerTodosLosAnimales();

        assertEquals(1, result.size());
        verify(animalRepository).findAll();
    }

    @Test
    void obtenerTodosLosAnimales_vacio_lanzaExcepcion() {
        when(animalRepository.findAll()).thenReturn(List.of());

        assertThrows(NoDataFoundException.class, () -> animalService.obtenerTodosLosAnimales());
    }

//    @Test
//    void crearAnimal_exitoso() {
//        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));
//
//        animalService.crearAnimal(requestDto);
//
//        verify(animalRepository).save(any(Animal.class));
//    }

    @Test
    void crearAnimal_especieNoEncontrada_lanzaExcepcion() {
        when(especieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class, () -> animalService.crearAnimal(requestDto));
    }

    @Test
    void editarAnimal_exitoso() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie));

        animalService.editarAnimal(1L, requestDto);

        verify(animalRepository).save(animal);
        assertEquals("Simba", animal.getNombre());
    }

    @Test
    void editarAnimal_noEncontrado_lanzaExcepcion() {
        when(animalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class, () -> animalService.editarAnimal(1L, requestDto));
    }

    @Test
    void eliminarAnimal_exitoso() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));

        animalService.eliminarAnimal(1L);

        verify(animalRepository).delete(animal);
    }

    @Test
    void eliminarAnimal_noEncontrado_lanzaExcepcion() {
        when(animalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class, () -> animalService.eliminarAnimal(1L));
    }

    @Test
    void obtenerAnimalesRegistradosEnFecha_exitoso() {
        LocalDate fecha = LocalDate.of(2025, 1, 1);
        when(animalRepository.findByFechaIngresoBetween(
                fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX)))
                .thenReturn(List.of(animal));

        List<AnimalRegistradoResponseDto> result = animalService.obtenerAnimalesRegistradosEnFecha(fecha);

        assertEquals(1, result.size());
        assertEquals("Simba", result.get(0).getNombreAnimal());
    }

    @Test
    void obtenerAnimalesRegistradosEnFecha_vacio_lanzaExcepcion() {
        LocalDate fecha = LocalDate.of(2025, 1, 1);
        when(animalRepository.findByFechaIngresoBetween(
                fecha.atStartOfDay(), fecha.atTime(LocalTime.MAX)))
                .thenReturn(List.of());

        assertThrows(AnimalesNoEncontradosEnFechaException.class,
                () -> animalService.obtenerAnimalesRegistradosEnFecha(fecha));
    }
}
