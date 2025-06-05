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
import com.playko.zoologico.exception.zona.ZonaEspecieMismatchException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    private Zona zona;
    private Especie especie;
    private Animal animal;

    @BeforeEach
    void setUp() {
        // Creamos una Zona de ejemplo
        zona = new Zona();
        zona.setId(1L);
        zona.setNombre("Zona Test");

        // Creamos una Especie de ejemplo asociada a la Zona
        especie = new Especie();
        especie.setId(2L);
        especie.setNombre("Especie Test");
        especie.setZona(zona);

        // Creamos un Animal de ejemplo asociado a la Especie (y por ende a la Zona)
        animal = new Animal();
        animal.setId(3L);
        animal.setNombre("Animal Test");
        animal.setFechaIngreso(LocalDateTime.now());
        animal.setEspecie(especie);
    }

    // ========== obtenerAnimalPorId ==========

    @Test
    void testObtenerAnimalPorId_Success() {
        when(animalRepository.findById(3L)).thenReturn(Optional.of(animal));

        AnimalResponseDto dto = animalService.obtenerAnimalPorId(3L);

        assertNotNull(dto);
        assertEquals(animal.getId(), dto.getId());
        assertEquals(animal.getNombre(), dto.getNombre());
        assertEquals(especie.getNombre(), dto.getEspecieName());
        verify(animalRepository, times(1)).findById(3L);
    }

    @Test
    void testObtenerAnimalPorId_NotFound() {
        when(animalRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class, () -> animalService.obtenerAnimalPorId(3L));
        verify(animalRepository, times(1)).findById(3L);
    }

    // ========== obtenerTodosLosAnimales ==========

    @Test
    void testObtenerTodosLosAnimales_Success() {
        Animal otroAnimal = new Animal();
        otroAnimal.setId(4L);
        otroAnimal.setNombre("Otro Animal");
        otroAnimal.setFechaIngreso(LocalDateTime.now());
        otroAnimal.setEspecie(especie);

        when(animalRepository.findAll()).thenReturn(List.of(animal, otroAnimal));

        List<AnimalResponseDto> list = animalService.obtenerTodosLosAnimales();

        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(dto -> dto.getId().equals(3L)));
        assertTrue(list.stream().anyMatch(dto -> dto.getId().equals(4L)));
        verify(animalRepository, times(1)).findAll();
    }

    @Test
    void testObtenerTodosLosAnimales_NoData() {
        when(animalRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(NoDataFoundException.class, () -> animalService.obtenerTodosLosAnimales());
        verify(animalRepository, times(1)).findAll();
    }

    // ========== crearAnimal ==========

    @Test
    void testCrearAnimal_Success() {
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("Nuevo Animal");
        request.setEspecieName(especie.getNombre());
        // Forzamos una fecha fija (por ejemplo, 1 de enero de 2021 a las 12:00)
        request.setFechaIngreso(LocalDateTime.of(2021, 1, 1, 12, 0));

        when(especieRepository.findByNombreIgnoreCase(especie.getNombre()))
                .thenReturn(Optional.of(especie));
        when(zonaRepository.findByNombreIgnoreCase(zona.getNombre()))
                .thenReturn(Optional.of(zona));
        // Simulamos que al llamar a save, simplemente devuelve la entidad que se guardó
        when(animalRepository.save(any(Animal.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> animalService.crearAnimal(request));

        verify(especieRepository, times(1)).findByNombreIgnoreCase(especie.getNombre());
        verify(zonaRepository, times(1)).findByNombreIgnoreCase(zona.getNombre());
        verify(animalRepository, times(1)).save(any(Animal.class));
    }

    @Test
    void testCrearAnimal_EspecieNotFound() {
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("Nuevo Animal");
        request.setEspecieName("NoExiste");

        when(especieRepository.findByNombreIgnoreCase("NoExiste"))
                .thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class, () -> animalService.crearAnimal(request));
        verify(especieRepository, times(1)).findByNombreIgnoreCase("NoExiste");
        verify(zonaRepository, never()).findByNombreIgnoreCase(any());
        verify(animalRepository, never()).save(any());
    }

    @Test
    void testCrearAnimal_ZonaNotFound() {
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("Nuevo Animal");
        request.setEspecieName(especie.getNombre());

        when(especieRepository.findByNombreIgnoreCase(especie.getNombre()))
                .thenReturn(Optional.of(especie));
        when(zonaRepository.findByNombreIgnoreCase(zona.getNombre()))
                .thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> animalService.crearAnimal(request));
        verify(especieRepository, times(1)).findByNombreIgnoreCase(especie.getNombre());
        verify(zonaRepository, times(1)).findByNombreIgnoreCase(zona.getNombre());
        verify(animalRepository, never()).save(any());
    }

    @Test
    void testCrearAnimal_ZonaEspecieMismatch() {
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("Nuevo Animal");
        request.setEspecieName(especie.getNombre());

        // Creamos una zona diferente a la que tiene la especie
        Zona otraZona = new Zona();
        otraZona.setId(99L);
        otraZona.setNombre("Otra Zona");

        when(especieRepository.findByNombreIgnoreCase(especie.getNombre()))
                .thenReturn(Optional.of(especie));
        when(zonaRepository.findByNombreIgnoreCase(zona.getNombre()))
                .thenReturn(Optional.of(otraZona));

        assertThrows(ZonaEspecieMismatchException.class, () -> animalService.crearAnimal(request));
        verify(especieRepository, times(1)).findByNombreIgnoreCase(especie.getNombre());
        verify(zonaRepository, times(1)).findByNombreIgnoreCase(zona.getNombre());
        verify(animalRepository, never()).save(any());
    }

    // ========== editarAnimal ==========

    @Test
    void testEditarAnimal_Success() {
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("Animal Editado");
        request.setEspecieName("Especie Nueva");

        // Creamos un animal actual y una nueva especie válida
        Animal currentAnimal = new Animal();
        currentAnimal.setId(3L);
        currentAnimal.setNombre("Animal Test");
        currentAnimal.setFechaIngreso(LocalDateTime.now());
        currentAnimal.setEspecie(especie);

        Especie nuevaEspecie = new Especie();
        nuevaEspecie.setId(5L);
        nuevaEspecie.setNombre("Especie Nueva");
        nuevaEspecie.setZona(zona);

        when(animalRepository.findById(3L)).thenReturn(Optional.of(currentAnimal));
        when(especieRepository.findByNombreIgnoreCase("Especie Nueva"))
                .thenReturn(Optional.of(nuevaEspecie));
        when(animalRepository.save(any(Animal.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> animalService.editarAnimal(3L, request));
        verify(animalRepository, times(1)).findById(3L);
        verify(especieRepository, times(1)).findByNombreIgnoreCase("Especie Nueva");
        verify(animalRepository, times(1)).save(any(Animal.class));
    }

    @Test
    void testEditarAnimal_AnimalNotFound() {
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("Animal Editado");
        request.setEspecieName("Especie Nueva");

        when(animalRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class, () -> animalService.editarAnimal(3L, request));
        verify(animalRepository, times(1)).findById(3L);
        verify(especieRepository, never()).findByNombreIgnoreCase(any());
        verify(animalRepository, never()).save(any());
    }

    @Test
    void testEditarAnimal_EspecieNotFound() {
        AnimalRequestDto request = new AnimalRequestDto();
        request.setNombre("Animal Editado");
        request.setEspecieName("NoExiste");

        when(animalRepository.findById(3L)).thenReturn(Optional.of(animal));
        when(especieRepository.findByNombreIgnoreCase("NoExiste"))
                .thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class, () -> animalService.editarAnimal(3L, request));
        verify(animalRepository, times(1)).findById(3L);
        verify(especieRepository, times(1)).findByNombreIgnoreCase("NoExiste");
        verify(animalRepository, never()).save(any());
    }

    // ========== eliminarAnimal ==========

    @Test
    void testEliminarAnimal_Success() {
        when(animalRepository.findById(3L)).thenReturn(Optional.of(animal));
        doNothing().when(animalRepository).delete(animal);

        assertDoesNotThrow(() -> animalService.eliminarAnimal(3L));
        verify(animalRepository, times(1)).findById(3L);
        verify(animalRepository, times(1)).delete(animal);
    }

    @Test
    void testEliminarAnimal_NotFound() {
        when(animalRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class, () -> animalService.eliminarAnimal(3L));
        verify(animalRepository, times(1)).findById(3L);
        verify(animalRepository, never()).delete(any());
    }

    // ========== obtenerAnimalesRegistradosEnFecha ==========

    @Test
    void testObtenerAnimalesRegistradosEnFecha_Success() {
        LocalDate fecha = LocalDate.of(2022, 5, 10);
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        Animal registrado = new Animal();
        registrado.setId(6L);
        registrado.setNombre("Animal Registrado");
        registrado.setFechaIngreso(LocalDateTime.of(2022, 5, 10, 10, 0));
        registrado.setEspecie(especie);

        when(animalRepository.findByFechaIngresoBetween(inicio, fin))
                .thenReturn(List.of(registrado));

        List<AnimalRegistradoResponseDto> list = animalService.obtenerAnimalesRegistradosEnFecha(fecha);

        assertEquals(1, list.size());
        assertEquals("Animal Registrado", list.get(0).getNombreAnimal());
        assertEquals(especie.getNombre(), list.get(0).getEspecie());
        verify(animalRepository, times(1)).findByFechaIngresoBetween(inicio, fin);
    }

    @Test
    void testObtenerAnimalesRegistradosEnFecha_NoData() {
        LocalDate fecha = LocalDate.of(2022, 5, 10);
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        when(animalRepository.findByFechaIngresoBetween(inicio, fin))
                .thenReturn(Collections.emptyList());

        assertThrows(AnimalesNoEncontradosEnFechaException.class, () ->
                animalService.obtenerAnimalesRegistradosEnFecha(fecha)
        );
        verify(animalRepository, times(1)).findByFechaIngresoBetween(inicio, fin);
    }
}
