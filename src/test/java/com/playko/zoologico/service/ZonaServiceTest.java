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
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.impl.ZonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

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

    @Mock
    private IEspecieRepository especieRepository;

    @InjectMocks
    private ZonaService zonaService;

    private Zona zona;
    private Especie especie;
    private Animal animal;

    @BeforeEach
    void setUp() {
        animal = new Animal();
        animal.setId(1L);

        especie = new Especie();
        especie.setId(10L);
        especie.setAnimales(Set.of(animal));

        zona = new Zona();
        zona.setId(100L);
        zona.setNombre("Zona Norte");
        zona.setEspecies(Set.of(especie));
    }

//    @Test
//    void obtenerZonaPorId_debeRetornarDtoCuandoExiste() {
//        when(zonaRepository.findById(100L)).thenReturn(Optional.of(zona));
//
//        ZonaResponseDto result = zonaService.obtenerZonaPorId(100L);
//
//        assertNotNull(result);
//        assertEquals(100L, result.getId());
//        assertEquals("Zona Norte", result.getNombre());
//        assertEquals(List.of(10L), result.getEspeciesIds());
//        assertEquals(List.of(1L), result.getAnimalesIds());
//    }

    @Test
    void obtenerZonaPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(zonaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> zonaService.obtenerZonaPorId(999L));
    }

//    @Test
//    void obtenerTodasLasZonas_debeRetornarListaCuandoExisten() {
//        when(zonaRepository.findAllWithEspeciesAndAnimales()).thenReturn(List.of(zona));
//
//        List<ZonaResponseDto> result = zonaService.obtenerTodasLasZonas();
//
//        assertEquals(1, result.size());
//        assertEquals("Zona Norte", result.get(0).getNombre());
//        verify(zonaRepository).findAllWithEspeciesAndAnimales();
//    }
//
//    @Test
//    void obtenerTodasLasZonas_debeLanzarExcepcionCuandoNoHayDatos() {
//        when(zonaRepository.findAllWithEspeciesAndAnimales()).thenReturn(List.of());
//
//        assertThrows(NoDataFoundException.class, () -> zonaService.obtenerTodasLasZonas());
//    }

    @Test
    void crearZona_debeGuardarNuevaZona() {
        ZonaRequestDto dto = new ZonaRequestDto(null, "  Nueva Zona  ");

        zonaService.crearZona(dto);

        verify(zonaRepository).save(argThat(z -> z.getNombre().equals("Nueva Zona")));
    }

    @Test
    void editarZona_debeActualizarNombreSiEsDistinto() {
        when(zonaRepository.findById(100L)).thenReturn(Optional.of(zona));
        ZonaRequestDto dto = new ZonaRequestDto(100L, "Zona Actualizada");

        zonaService.editarZona(100L, dto);

        verify(zonaRepository).save(argThat(z -> z.getNombre().equals("Zona Actualizada")));
    }

    @Test
    void editarZona_noDebeActualizarSiElNombreEsIgualIgnorandoMayusculas() {
        when(zonaRepository.findById(100L)).thenReturn(Optional.of(zona));
        ZonaRequestDto dto = new ZonaRequestDto(100L, "zona norte");

        zonaService.editarZona(100L, dto);

        verify(zonaRepository).save(zona);
    }

    @Test
    void editarZona_debeLanzarExcepcionSiNoExiste() {
        when(zonaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> zonaService.editarZona(999L, new ZonaRequestDto()));
    }

    @Test
    void eliminarZona_debeEliminarCuandoNoHayAnimales() {
        when(zonaRepository.findById(100L)).thenReturn(Optional.of(zona));
        when(animalRepository.existsByEspecie_Zona(zona)).thenReturn(false);

        zonaService.eliminarZona(100L);

        verify(zonaRepository).delete(zona);
    }

    @Test
    void eliminarZona_debeLanzarExcepcionSiHayAnimales() {
        when(zonaRepository.findById(100L)).thenReturn(Optional.of(zona));
        when(animalRepository.existsByEspecie_Zona(zona)).thenReturn(true);

        assertThrows(ZonaConAnimalesException.class, () -> zonaService.eliminarZona(100L));
        verify(zonaRepository, never()).delete(any());
    }

    @Test
    void eliminarZona_debeLanzarExcepcionSiNoExiste() {
        when(zonaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> zonaService.eliminarZona(999L));
    }

    @Test
    void obtenerCantidadAnimalesPorZona_debeRetornarLista() {
        when(zonaRepository.findAll()).thenReturn(List.of(zona));

        List<CantidadAnimalesPorZonaResponseDto> result = zonaService.obtenerCantidadAnimalesPorZona();

        assertEquals(1, result.size());
        assertEquals("Zona Norte", result.get(0).getNombreZona());
        assertEquals(1L, result.get(0).getCantidadAnimales());
    }

    @Test
    void obtenerCantidadAnimalesPorZona_debeLanzarExcepcionSiNoHayZonas() {
        when(zonaRepository.findAll()).thenReturn(List.of());

        assertThrows(NoDataFoundException.class, () -> zonaService.obtenerCantidadAnimalesPorZona());
    }
}