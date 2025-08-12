package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.EspecieRequestDto;
import com.playko.zoologico.dto.response.AnimalesPorEspecieResponseDto;
import com.playko.zoologico.dto.response.EspecieResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.especie.EspecieConAnimalesException;
import com.playko.zoologico.exception.especie.EspecieNotFoundException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.impl.EspecieService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EspecieServiceTest {

    @Mock
    private IEspecieRepository especieRepository;

    @Mock
    private IZonaRepository zonaRepository;

    @Mock
    private IAnimalRepository animalRepository;

    @InjectMocks
    private EspecieService especieService;

    private Zona zona;
    private Especie especie;
    private Animal animal;

    @BeforeEach
    void setUp() {
        zona = new Zona();
        zona.setId(1L);
        zona.setNombre("Zona Norte");

        animal = new Animal();
        animal.setId(100L);

        especie = new Especie();
        especie.setId(10L);
        especie.setNombre("León");
        especie.setZona(zona);
        especie.setAnimales(Set.of(animal));
    }

    @Test
    void obtenerEspeciePorId_debeRetornarDtoCuandoExiste() {
        when(especieRepository.findById(10L)).thenReturn(Optional.of(especie));

        EspecieResponseDto result = especieService.obtenerEspeciePorId(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals("León", result.getNombre());
        assertEquals(1L, result.getZonaId());
        assertEquals(List.of(100L), result.getAnimalesIds());
    }

    @Test
    void obtenerEspeciePorId_debeLanzarExcepcionCuandoNoExiste() {
        when(especieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class,
                () -> especieService.obtenerEspeciePorId(999L));
    }

    @Test
    void obtenerTodasLasEspecies_debeRetornarListaCuandoExisten() {
        when(especieRepository.findAll()).thenReturn(List.of(especie));

        List<EspecieResponseDto> result = especieService.obtenerTodasLasEspecies();

        assertEquals(1, result.size());
        assertEquals("León", result.get(0).getNombre());
    }

    @Test
    void obtenerTodasLasEspecies_debeLanzarExcepcionSiNoHayDatos() {
        when(especieRepository.findAll()).thenReturn(List.of());

        assertThrows(NoDataFoundException.class,
                () -> especieService.obtenerTodasLasEspecies());
    }

    @Test
    void crearEspecie_debeGuardarEspecieCuandoZonaExiste() {
        EspecieRequestDto dto = new EspecieRequestDto("  Tigre  ", 1L);
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));

        especieService.crearEspecie(dto);

        verify(especieRepository).save(argThat(e ->
                e.getNombre().equals("Tigre") && e.getZona().equals(zona)
        ));
    }

    @Test
    void crearEspecie_debeLanzarExcepcionSiZonaNoExiste() {
        EspecieRequestDto dto = new EspecieRequestDto("Tigre", 99L);
        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class,
                () -> especieService.crearEspecie(dto));
    }

    @Test
    void editarEspecie_debeActualizarNombreYZona() {
        EspecieRequestDto dto = new EspecieRequestDto("Leopardo", 1L);
        when(especieRepository.findById(10L)).thenReturn(Optional.of(especie));
        when(zonaRepository.findById(1L)).thenReturn(Optional.of(zona));

        especieService.editarEspecie(10L, dto);

        verify(especieRepository).save(argThat(e ->
                e.getNombre().equals("Leopardo") && e.getZona().equals(zona)
        ));
    }

    @Test
    void editarEspecie_debeLanzarExcepcionSiEspecieNoExiste() {
        EspecieRequestDto dto = new EspecieRequestDto("Jaguar", 1L);
        when(especieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class,
                () -> especieService.editarEspecie(99L, dto));
    }

    @Test
    void editarEspecie_debeLanzarExcepcionSiZonaNoExiste() {
        EspecieRequestDto dto = new EspecieRequestDto("Jaguar", 99L);
        when(especieRepository.findById(10L)).thenReturn(Optional.of(especie));
        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class,
                () -> especieService.editarEspecie(10L, dto));
    }

    @Test
    void eliminarEspecie_debeEliminarCuandoNoTieneAnimales() {
        when(especieRepository.findById(10L)).thenReturn(Optional.of(especie));
        when(animalRepository.existsByEspecie(especie)).thenReturn(false);

        especieService.eliminarEspecie(10L);

        verify(especieRepository).delete(especie);
    }

    @Test
    void eliminarEspecie_debeLanzarExcepcionSiTieneAnimales() {
        when(especieRepository.findById(10L)).thenReturn(Optional.of(especie));
        when(animalRepository.existsByEspecie(especie)).thenReturn(true);

        assertThrows(EspecieConAnimalesException.class,
                () -> especieService.eliminarEspecie(10L));

        verify(especieRepository, never()).delete(any());
    }

    @Test
    void eliminarEspecie_debeLanzarExcepcionSiNoExiste() {
        when(especieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class,
                () -> especieService.eliminarEspecie(99L));
    }

    @Test
    void obtenerCantidadAnimalesPorEspecie_debeRetornarLista() {
        when(especieRepository.findAll()).thenReturn(List.of(especie));

        List<AnimalesPorEspecieResponseDto> result = especieService.obtenerCantidadAnimalesPorEspecie();

        assertEquals(1, result.size());
        assertEquals("León", result.get(0).getEspecie());
        assertEquals(1, result.get(0).getCantidadAnimales());
    }

    @Test
    void obtenerCantidadAnimalesPorEspecie_debeLanzarExcepcionSiNoHayEspecies() {
        when(especieRepository.findAll()).thenReturn(List.of());

        assertThrows(NoDataFoundException.class,
                () -> especieService.obtenerCantidadAnimalesPorEspecie());
    }
}



