package com.playko.zoologico.service.services;

import com.playko.zoologico.dto.request.ZonaRequestDto;
import com.playko.zoologico.dto.response.CantidadAnimalesPorZonaResponseDto;
import com.playko.zoologico.dto.response.IdAndEspecieResponseDto;
import com.playko.zoologico.dto.response.ZonaResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.NonNegativePageNumberException;
import com.playko.zoologico.exception.animal.ZonaConAnimalesException;
import com.playko.zoologico.exception.zona.IdZonaInvalidException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.impl.ZonaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    private Zona zona1;
    private Zona zona2;
    private Especie especieA;
    private Especie especieB;
    private Animal animal1;
    private Animal animal2;
    private Animal animal3;

    @BeforeEach
    void setUp() {
        // animales
        animal1 = new Animal(); animal1.setId(101L); animal1.setNombre("A1");
        animal2 = new Animal(); animal2.setId(102L); animal2.setNombre("A2");
        animal3 = new Animal(); animal3.setId(103L); animal3.setNombre("A3");

        // especies (usa Sets porque Zona.getEspecies devuelve Set)
        especieA = new Especie();
        especieA.setId(11L);
        especieA.setNombre("EspecieA");
        // especies pueden tener conjunto de animales (usa Set para compatibilidad con mapToResponseDto)
        especieA.setAnimales(new HashSet<>(Set.of(animal1, animal2)));

        especieB = new Especie();
        especieB.setId(12L);
        especieB.setNombre("EspecieB");
        especieB.setAnimales(new HashSet<>(Set.of(animal3)));

        // zonas
        zona1 = new Zona();
        zona1.setId(1L);
        zona1.setNombre("Zona Uno");
        zona1.setEspecies(new HashSet<>(Set.of(especieA, especieB)));

        zona2 = new Zona();
        zona2.setId(2L);
        zona2.setNombre("Zona Dos");
        zona2.setEspecies(null); // cubre rama especies == null
    }

    /* ========== obtenerZonaPorId ========== */

    @Test
    void obtenerZonaPorId_shouldThrow_whenIdIsNull() {
        assertThrows(IdZonaInvalidException.class, () -> zonaService.obtenerZonaPorId(null));
    }

    @Test
    void obtenerZonaPorId_shouldThrow_whenIdIsZeroOrNegative() {
        assertThrows(IdZonaInvalidException.class, () -> zonaService.obtenerZonaPorId(0L));
        assertThrows(IdZonaInvalidException.class, () -> zonaService.obtenerZonaPorId(-5L));
    }

    @Test
    void obtenerZonaPorId_shouldThrow_whenNotFound() {
        when(zonaRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ZonaNotFoundException.class, () -> zonaService.obtenerZonaPorId(5L));
    }

    @Test
    void obtenerZonaPorId_shouldReturnDto_withEspeciesAndCount() {
        when(zonaRepository.findById(zona1.getId())).thenReturn(Optional.of(zona1));

        ZonaResponseDto dto = zonaService.obtenerZonaPorId(zona1.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.getNombre()).isEqualTo(zona1.getNombre());
        // especies mapeadas a IdAndEspecieResponseDto
        List<IdAndEspecieResponseDto> especiesDto = dto.getEspecies();
        assertThat(especiesDto).hasSize(2);
        // cantidad total animales = 3 (2 + 1)
        assertThat(dto.getCantidadAnimales()).isEqualTo(3L);
    }

    @Test
    void obtenerZonaPorId_shouldReturnDto_whenEspeciesNull() {
        when(zonaRepository.findById(zona2.getId())).thenReturn(Optional.of(zona2));

        ZonaResponseDto dto = zonaService.obtenerZonaPorId(zona2.getId());

        assertThat(dto).isNotNull();
        assertThat(dto.getNombre()).isEqualTo(zona2.getNombre());
        assertThat(dto.getEspecies()).isEmpty();
        assertThat(dto.getCantidadAnimales()).isZero();
    }

    /* ========== obtenerTodasLasZonas ========== */

    @Test
    void obtenerTodasLasZonas_shouldThrow_whenPageNegative() {
        assertThrows(NonNegativePageNumberException.class, () -> zonaService.obtenerTodasLasZonas("", -1));
    }

    @Test
    void obtenerTodasLasZonas_shouldThrow_whenPageResultEmpty() {
        Pageable pageable = PageRequest.of(0, 5);
        Page<Zona> emptyPage = Page.empty(pageable);
        when(zonaRepository.findZonasPaginadas("", pageable)).thenReturn(emptyPage);

        assertThrows(NoDataFoundException.class, () -> zonaService.obtenerTodasLasZonas("", 0));
    }

    @Test
    void obtenerTodasLasZonas_shouldReturnPageMapped_whenSuccess() {
        // preparar page con dos zonas básicos (solo ids y nombres)
        Zona zA = new Zona(); zA.setId(1L); zA.setNombre("Z A");
        Zona zB = new Zona(); zB.setId(2L); zB.setNombre("Z B");
        Pageable pageable = PageRequest.of(0, 5);
        Page<Zona> page = new PageImpl<>(List.of(zA, zB), pageable, 2);

        // la llamada paginada devuelve zA,zB
        when(zonaRepository.findZonasPaginadas("", pageable)).thenReturn(page);

        // fetch con relaciones: devolvemos versiones completas con especies/animales
        Zona fetchedA = zona1; // previamente preparado (tiene especies y animales)
        Zona fetchedB = zona2; // tiene especies null
        when(zonaRepository.findZonasWithFetchByIds(List.of(1L, 2L))).thenReturn(List.of(fetchedA, fetchedB));

        Page<ZonaResponseDto> result = zonaService.obtenerTodasLasZonas("", 0);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(2);
        List<ZonaResponseDto> content = result.getContent();
        assertThat(content).hasSize(2);
        // el mapping usa zonasMap.get(id) — comprobamos que el primer dto corresponde a fetchedA
        ZonaResponseDto dtoA = content.stream().filter(d -> d.getNombre().equals(fetchedA.getNombre())).findFirst().orElse(null);
        assertThat(dtoA).isNotNull();
        assertThat(dtoA.getCantidadAnimales()).isEqualTo(3L);
    }

    /* ========== crearZona ========== */

    @Test
    void crearZona_shouldTrimAndSave() {
        ZonaRequestDto req = new ZonaRequestDto();
        req.setNombre("   Nueva Zona   ");

        zonaService.crearZona(req);

        ArgumentCaptor<Zona> captor = ArgumentCaptor.forClass(Zona.class);
        verify(zonaRepository).save(captor.capture());
        Zona saved = captor.getValue();
        assertThat(saved.getNombre()).isEqualTo("Nueva Zona");
    }

    /* ========== editarZona ========== */

    @Test
    void editarZona_shouldThrow_whenNotFound() {
        when(zonaRepository.findById(99L)).thenReturn(Optional.empty());
        ZonaRequestDto req = new ZonaRequestDto(); req.setNombre("X");
        assertThrows(ZonaNotFoundException.class, () -> zonaService.editarZona(99L, req));
        verify(zonaRepository, never()).save(any());
    }

    @Test
    void editarZona_shouldUpdateName_whenDifferentIgnoringCase() {
        Zona existing = new Zona();
        existing.setId(5L);
        existing.setNombre("Zona Vieja");

        when(zonaRepository.findById(5L)).thenReturn(Optional.of(existing));
        ZonaRequestDto req = new ZonaRequestDto(); req.setNombre("  Nueva Zona  ");

        zonaService.editarZona(5L, req);

        ArgumentCaptor<Zona> captor = ArgumentCaptor.forClass(Zona.class);
        verify(zonaRepository).save(captor.capture());
        Zona saved = captor.getValue();
        assertThat(saved.getNombre()).isEqualTo("Nueva Zona");
    }

    @Test
    void editarZona_shouldNotChangeName_whenSameIgnoringCase() {
        Zona existing = new Zona();
        existing.setId(6L);
        existing.setNombre("ZonaX");

        when(zonaRepository.findById(6L)).thenReturn(Optional.of(existing));
        ZonaRequestDto req = new ZonaRequestDto(); req.setNombre("  zonax  "); // same ignoring case

        zonaService.editarZona(6L, req);

        ArgumentCaptor<Zona> captor = ArgumentCaptor.forClass(Zona.class);
        verify(zonaRepository).save(captor.capture());
        Zona saved = captor.getValue();
        // name should remain equal to original (trimmed input equals ignoring case but code doesn't set new name)
        assertThat(saved.getNombre()).isEqualTo("ZonaX");
    }

    /* ========== eliminarZona ========== */

    @Test
    void eliminarZona_shouldThrow_whenNotFound() {
        when(zonaRepository.findById(77L)).thenReturn(Optional.empty());
        assertThrows(ZonaNotFoundException.class, () -> zonaService.eliminarZona(77L));
        verify(animalRepository, never()).existsByEspecie_Zona(any());
    }

    @Test
    void eliminarZona_shouldThrow_whenHasAnimals() {
        when(zonaRepository.findById(zona1.getId())).thenReturn(Optional.of(zona1));
        when(animalRepository.existsByEspecie_Zona(zona1)).thenReturn(true);

        Runnable action = () -> zonaService.eliminarZona(zona1.getId());

        assertThrows(ZonaConAnimalesException.class, action::run);
        verify(zonaRepository, never()).delete(any());
    }


    @Test
    void eliminarZona_shouldDelete_whenNoAnimals() {
        when(zonaRepository.findById(zona2.getId())).thenReturn(Optional.of(zona2));
        when(animalRepository.existsByEspecie_Zona(zona2)).thenReturn(false);

        zonaService.eliminarZona(zona2.getId());

        verify(zonaRepository).delete(zona2);
    }

    /* ========== obtenerCantidadAnimalesPorZona ========== */

    @Test
    void obtenerCantidadAnimalesPorZona_shouldThrow_whenNoZones() {
        when(zonaRepository.findAll()).thenReturn(List.of());
        assertThrows(NoDataFoundException.class, () -> zonaService.obtenerCantidadAnimalesPorZona());
    }

    @Test
    void obtenerCantidadAnimalesPorZona_shouldReturnCounts_correctly() {
        // zona1 tiene 3 animales (especieA:2, especieB:1)
        // zona2 tiene especies == null -> 0
        when(zonaRepository.findAll()).thenReturn(List.of(zona1, zona2));

        List<CantidadAnimalesPorZonaResponseDto> res = zonaService.obtenerCantidadAnimalesPorZona();

        assertThat(res).hasSize(2);
        CantidadAnimalesPorZonaResponseDto r1 = res.stream().filter(r -> r.getNombreZona().equals(zona1.getNombre())).findFirst().orElseThrow();
        CantidadAnimalesPorZonaResponseDto r2 = res.stream().filter(r -> r.getNombreZona().equals(zona2.getNombre())).findFirst().orElseThrow();

        assertThat(r1.getCantidadAnimales()).isEqualTo(3L);
        assertThat(r2.getCantidadAnimales()).isZero();
    }
}