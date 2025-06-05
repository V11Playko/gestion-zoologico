package com.playko.zoologico.service;

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
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.impl.ZonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZonaServiceTest {

    @Mock
    private IZonaRepository zonaRepository;
    @Mock
    private IAnimalRepository animalRepository;

    @InjectMocks
    private ZonaService zonaService;

    private Zona zona;
    private Especie especie1;
    private Animal animalA;
    private Animal animalB;

    @BeforeEach
    void setUp() {
        // Configuramos una zona de ejemplo
        zona = new Zona();
        zona.setId(1L);
        zona.setNombre("Zona Test");
        zona.setEspecies(Collections.emptyList());

        // Configuramos una especie con animales
        especie1 = new Especie();
        especie1.setId(2L);
        especie1.setNombre("Especie Test");
        especie1.setZona(zona);

        animalA = new Animal();
        animalA.setId(3L);
        animalA.setNombre("Animal A");
        animalA.setEspecie(especie1);

        animalB = new Animal();
        animalB.setId(4L);
        animalB.setNombre("Animal B");
        animalB.setEspecie(especie1);

        // Asociamos animales a la especie
        especie1.setAnimales(List.of(animalA, animalB));
        // Asociamos especie a la zona
        zona.setEspecies(List.of(especie1));
    }

    // ====== obtenerZonaPorId ======

    @Test
    void testObtenerZonaPorId_Success() {
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));

        ZonaResponseDto dto = zonaService.obtenerZonaPorId(1L);

        assertNotNull(dto);
        assertEquals(zona.getId(), dto.getId());
        assertEquals("Zona Test", dto.getNombre());
        // La zona tiene una única especie "Especie Test"
        assertEquals(List.of("Especie Test"), dto.getEspecies());
        // La zona tiene dos animales ("Animal A", "Animal B")
        assertTrue(dto.getNameAnimales().containsAll(List.of("Animal A", "Animal B")));
        verify(zonaRepository, times(1)).findById(1L);
    }

    @Test
    void testObtenerZonaPorId_NotFound() {
        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () ->
                zonaService.obtenerZonaPorId(99L)
        );
        verify(zonaRepository, times(1)).findById(99L);
    }

    // ====== obtenerTodasLasZonas ======

    @Test
    void testObtenerTodasLasZonas_Success() {
        Zona otraZona = new Zona();
        otraZona.setId(5L);
        otraZona.setNombre("Otra Zona");
        otraZona.setEspecies(Collections.emptyList());

        when(zonaRepository.findAll()).thenReturn(List.of(zona, otraZona));

        List<ZonaResponseDto> lista = zonaService.obtenerTodasLasZonas();

        assertEquals(2, lista.size());
        assertTrue(lista.stream().anyMatch(dto -> dto.getNombre().equals("Zona Test")));
        assertTrue(lista.stream().anyMatch(dto -> dto.getNombre().equals("Otra Zona")));
        verify(zonaRepository, times(1)).findAll();
    }

    @Test
    void testObtenerTodasLasZonas_NoData() {
        when(zonaRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(NoDataFoundException.class, () ->
                zonaService.obtenerTodasLasZonas()
        );
        verify(zonaRepository, times(1)).findAll();
    }

    // ====== crearZona ======

    @Test
    void testCrearZona_Success() {
        ZonaRequestDto request = new ZonaRequestDto();
        request.setNombre("Nueva Zona");

        when(zonaRepository.existsByNombreIgnoreCase("Nueva Zona")).thenReturn(false);
        when(zonaRepository.save(any(Zona.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> zonaService.crearZona(request));

        verify(zonaRepository, times(1)).existsByNombreIgnoreCase("Nueva Zona");
        verify(zonaRepository, times(1)).save(argThat(savedZona ->
                savedZona.getNombre().equals("Nueva Zona")
        ));
    }

    @Test
    void testCrearZona_AlreadyExists() {
        ZonaRequestDto request = new ZonaRequestDto();
        request.setNombre("Zona Test");

        when(zonaRepository.existsByNombreIgnoreCase("Zona Test")).thenReturn(true);

        assertThrows(ZonaAlreadyExistsException.class, () ->
                zonaService.crearZona(request)
        );
        verify(zonaRepository, times(1)).existsByNombreIgnoreCase("Zona Test");
        verify(zonaRepository, never()).save(any());
    }

    // ====== editarZona ======

    @Test
    void testEditarZona_Success_ChangeName() {
        ZonaRequestDto request = new ZonaRequestDto();
        request.setNombre("Zona Modificada");

        Zona existente = new Zona();
        existente.setId(1L);
        existente.setNombre("Zona Test");
        existente.setEspecies(Collections.emptyList());

        when(zonaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(zonaRepository.existsByNombreIgnoreCase("Zona Modificada")).thenReturn(false);
        when(zonaRepository.save(any(Zona.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> zonaService.editarZona(1L, request));

        verify(zonaRepository, times(1)).findById(1L);
        verify(zonaRepository, times(1)).existsByNombreIgnoreCase("Zona Modificada");
        verify(zonaRepository, times(1)).save(argThat(z ->
                z.getNombre().equals("Zona Modificada")
        ));
    }

    @Test
    void testEditarZona_Success_SameName() {
        ZonaRequestDto request = new ZonaRequestDto();
        request.setNombre("Zona Test"); // mismo nombre actual

        Zona existente = new Zona();
        existente.setId(1L);
        existente.setNombre("Zona Test");
        existente.setEspecies(Collections.emptyList());

        when(zonaRepository.findById(1L)).thenReturn(Optional.of(existente));
        // No se llama a existsByNombre porque el nombre no cambia
        when(zonaRepository.save(any(Zona.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> zonaService.editarZona(1L, request));

        verify(zonaRepository, times(1)).findById(1L);
        verify(zonaRepository, never()).existsByNombreIgnoreCase(any());
        verify(zonaRepository, times(1)).save(any(Zona.class));
    }

    @Test
    void testEditarZona_NotFound() {
        ZonaRequestDto request = new ZonaRequestDto();
        request.setNombre("Zona Inexistente");

        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () ->
                zonaService.editarZona(99L, request)
        );
        verify(zonaRepository, times(1)).findById(99L);
        verify(zonaRepository, never()).save(any());
    }

    @Test
    void testEditarZona_NameCollision() {
        ZonaRequestDto request = new ZonaRequestDto();
        request.setNombre("Otra Zona");

        Zona existente = new Zona();
        existente.setId(1L);
        existente.setNombre("Zona Test");
        existente.setEspecies(Collections.emptyList());

        when(zonaRepository.findById(1L)).thenReturn(Optional.of(existente));
        // Simulamos que "Otra Zona" ya existe en otra zona
        when(zonaRepository.existsByNombreIgnoreCase("Otra Zona")).thenReturn(true);

        assertThrows(ZonaAlreadyExistsException.class, () ->
                zonaService.editarZona(1L, request)
        );
        verify(zonaRepository, times(1)).findById(1L);
        verify(zonaRepository, times(1)).existsByNombreIgnoreCase("Otra Zona");
        verify(zonaRepository, never()).save(any());
    }

    // ====== eliminarZona ======

    @Test
    void testEliminarZona_Success() {
        Zona existente = new Zona();
        existente.setId(1L);
        existente.setNombre("Zona Test");
        existente.setEspecies(List.of(especie1));

        when(zonaRepository.findById(1L)).thenReturn(Optional.of(existente));
        // Simulamos que no hay animales asociados a esta zona
        when(animalRepository.existsByEspecie_Zona(existente)).thenReturn(false);
        doNothing().when(zonaRepository).delete(existente);

        assertDoesNotThrow(() -> zonaService.eliminarZona(1L));

        verify(zonaRepository, times(1)).findById(1L);
        verify(animalRepository, times(1)).existsByEspecie_Zona(existente);
        verify(zonaRepository, times(1)).delete(existente);
    }

    @Test
    void testEliminarZona_NotFound() {
        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () ->
                zonaService.eliminarZona(99L)
        );
        verify(zonaRepository, times(1)).findById(99L);
        verify(animalRepository, never()).existsByEspecie_Zona(any());
        verify(zonaRepository, never()).delete(any());
    }

    @Test
    void testEliminarZona_HasAnimals() {
        Zona existente = new Zona();
        existente.setId(1L);
        existente.setNombre("Zona Test");
        existente.setEspecies(List.of(especie1));

        when(zonaRepository.findById(1L)).thenReturn(Optional.of(existente));
        // Simulamos que sí hay animales asociados
        when(animalRepository.existsByEspecie_Zona(existente)).thenReturn(true);

        assertThrows(ZonaConAnimalesException.class, () ->
                zonaService.eliminarZona(1L)
        );
        verify(zonaRepository, times(1)).findById(1L);
        verify(animalRepository, times(1)).existsByEspecie_Zona(existente);
        verify(zonaRepository, never()).delete(any());
    }

    // ====== obtenerCantidadAnimalesPorZona ======

    @Test
    void testObtenerCantidadAnimalesPorZona_Success() {
        // ya en setUp la zona tiene una especie con 2 animales
        when(zonaRepository.findAll()).thenReturn(List.of(zona));

        List<CantidadAnimalesPorZonaResponseDto> lista =
                zonaService.obtenerCantidadAnimalesPorZona();

        assertEquals(1, lista.size());
        CantidadAnimalesPorZonaResponseDto dto = lista.get(0);
        assertEquals("Zona Test", dto.getNombreZona());
        assertEquals(2L, dto.getCantidadAnimales());
        verify(zonaRepository, times(1)).findAll();
    }

    @Test
    void testObtenerCantidadAnimalesPorZona_NoData() {
        when(zonaRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(NoDataFoundException.class, () ->
                zonaService.obtenerCantidadAnimalesPorZona()
        );
        verify(zonaRepository, times(1)).findAll();
    }
}