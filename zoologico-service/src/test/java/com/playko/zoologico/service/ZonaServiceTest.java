package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.CantidadAnimalesPorZonaResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.animal.ZonaConAnimalesException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.impl.ZonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
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


    @Test
    void obtenerZonaPorId_debeLanzarExcepcionCuandoNoExiste() {
        when(zonaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> zonaService.obtenerZonaPorId(999L));
    }

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

        assertThrows(ZonaNotFoundException.class, this::callEditarZonaNotFound);
    }

    private void callEditarZonaNotFound() {
        zonaService.editarZona(999L, new ZonaRequestDto());
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