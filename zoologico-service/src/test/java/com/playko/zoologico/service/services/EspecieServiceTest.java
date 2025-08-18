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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    private Zona zona1;
    private Zona zona2;
    private Especie especie1;
    private Especie especie2;
    private Animal a1;
    private Animal a2;

    @BeforeEach
    void setUp() {
        zona1 = new Zona();
        zona1.setId(10L);
        zona1.setNombre("Zona Norte");

        zona2 = new Zona();
        zona2.setId(11L);
        zona2.setNombre("Zona Sur");

        a1 = new Animal();
        a1.setId(100L);
        a1.setNombre("Animal1");

        a2 = new Animal();
        a2.setId(101L);
        a2.setNombre("Animal2");

        especie1 = new Especie();
        especie1.setId(1L);
        especie1.setNombre("Perro");
        especie1.setZona(zona1);
        especie1.setAnimales(Set.of(a1, a2));

        especie2 = new Especie();
        especie2.setId(2L);
        especie2.setNombre("Gato");
        especie2.setZona(zona2);
        especie2.setAnimales(null); // cubre la rama animales == null
    }

    /* obtenerEspeciePorId */

    @Test
    void obtenerEspeciePorId_shouldReturnDto_whenFound() {
        when(especieRepository.findById(1L)).thenReturn(Optional.of(especie1));

        EspecieResponseDto dto = especieService.obtenerEspeciePorId(1L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(especie1.getId());
        assertThat(dto.getNombre()).isEqualTo(especie1.getNombre());
        assertThat(dto.getZonaId()).isEqualTo(zona1.getId());
        assertThat(dto.getAnimalesIds()).containsExactlyInAnyOrder(100L, 101L);
    }

    @Test
    void obtenerEspeciePorId_shouldThrow_whenNotFound() {
        when(especieRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EspecieNotFoundException.class, () -> especieService.obtenerEspeciePorId(99L));
    }

    /* obtenerTodasLasEspecies */

    @Test
    void obtenerTodasLasEspecies_shouldReturnList_whenNotEmpty() {
        when(especieRepository.findAll()).thenReturn(List.of(especie1, especie2));

        List<EspecieResponseDto> result = especieService.obtenerTodasLasEspecies();

        assertThat(result).hasSize(2);
        // especie1 animales -> 2 ids
        EspecieResponseDto r1 = result.stream().filter(r -> r.getId() == 1L).findFirst().orElseThrow();
        assertThat(r1.getAnimalesIds()).hasSize(2);
        // especie2 animales == null -> lista vacía
        EspecieResponseDto r2 = result.stream().filter(r -> r.getId() == 2L).findFirst().orElseThrow();
        assertThat(r2.getAnimalesIds()).isEmpty();
    }

    @Test
    void obtenerTodasLasEspecies_shouldThrow_whenEmpty() {
        when(especieRepository.findAll()).thenReturn(List.of());
        assertThrows(NoDataFoundException.class, () -> especieService.obtenerTodasLasEspecies());
    }

    /* crearEspecie */

    @Test
    void crearEspecie_shouldSaveTrimmedName_whenZonaExists() {
        EspecieRequestDto dto = EspecieRequestDto.builder()
                .nombre("  Nueva Especie  ")
                .zonaId(zona1.getId())
                .build();

        when(zonaRepository.findById(zona1.getId())).thenReturn(Optional.of(zona1));

        especieService.crearEspecie(dto);

        ArgumentCaptor<Especie> captor = ArgumentCaptor.forClass(Especie.class);
        verify(especieRepository).save(captor.capture());
        Especie saved = captor.getValue();

        assertThat(saved.getNombre()).isEqualTo("Nueva Especie"); // trimmed
        assertThat(saved.getZona()).isEqualTo(zona1);
    }

    @Test
    void crearEspecie_shouldThrow_whenZonaNotFound() {
        EspecieRequestDto dto = EspecieRequestDto.builder()
                .nombre("X")
                .zonaId(999L)
                .build();

        when(zonaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> especieService.crearEspecie(dto));
        verify(especieRepository, never()).save(any());
    }

    /* editarEspecie */

    @Test
    void editarEspecie_shouldUpdateAndSave_whenAllGood() {
        EspecieRequestDto dto = EspecieRequestDto.builder()
                .nombre("  NuevoNombre  ")
                .zonaId(zona2.getId())
                .build();

        when(especieRepository.findById(especie1.getId())).thenReturn(Optional.of(especie1));
        when(zonaRepository.findById(zona2.getId())).thenReturn(Optional.of(zona2));

        especieService.editarEspecie(especie1.getId(), dto);

        ArgumentCaptor<Especie> captor = ArgumentCaptor.forClass(Especie.class);
        verify(especieRepository).save(captor.capture());
        Especie saved = captor.getValue();

        assertThat(saved.getNombre()).isEqualTo("NuevoNombre");
        assertThat(saved.getZona()).isEqualTo(zona2);
    }

    @Test
    void editarEspecie_shouldThrow_whenEspecieNotFound() {
        EspecieRequestDto dto = EspecieRequestDto.builder()
                .nombre("X")
                .zonaId(zona1.getId())
                .build();

        when(especieRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class, () -> especieService.editarEspecie(999L, dto));
        verify(especieRepository, never()).save(any());
    }

    @Test
    void editarEspecie_shouldThrow_whenZonaNotFound() {
        EspecieRequestDto dto = EspecieRequestDto.builder()
                .nombre("X")
                .zonaId(777L)
                .build();

        when(especieRepository.findById(especie1.getId())).thenReturn(Optional.of(especie1));
        when(zonaRepository.findById(777L)).thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () -> especieService.editarEspecie(especie1.getId(), dto));
        verify(especieRepository, never()).save(any());
    }

    /* eliminarEspecie */

    @Test
    void eliminarEspecie_shouldDelete_whenNoAnimals() {
        when(especieRepository.findById(especie2.getId())).thenReturn(Optional.of(especie2));
        when(animalRepository.existsByEspecie(especie2)).thenReturn(false);

        especieService.eliminarEspecie(especie2.getId());

        verify(especieRepository).delete(especie2);
    }

    @Test
    void eliminarEspecie_shouldThrow_whenHasAnimals() {
        when(especieRepository.findById(especie1.getId())).thenReturn(Optional.of(especie1));
        when(animalRepository.existsByEspecie(especie1)).thenReturn(true);

        assertThrows(EspecieConAnimalesException.class, () -> especieService.eliminarEspecie(especie1.getId()));
        verify(especieRepository, never()).delete(any());
    }

    @Test
    void eliminarEspecie_shouldThrow_whenEspecieNotFound() {
        when(especieRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EspecieNotFoundException.class, () -> especieService.eliminarEspecie(999L));
        verify(animalRepository, never()).existsByEspecie(any());
    }

    /* obtenerCantidadAnimalesPorEspecie */

    @Test
    void obtenerCantidadAnimalesPorEspecie_shouldReturnCounts_whenNotEmpty() {
        Especie e1 = new Especie();
        e1.setId(10L);
        e1.setNombre("E1");
        e1.setAnimales(Set.of(a1, a2));

        Especie e2 = new Especie();
        e2.setId(11L);
        e2.setNombre("E2");
        e2.setAnimales(null);

        when(especieRepository.findAll()).thenReturn(List.of(e1, e2));

        List<AnimalesPorEspecieResponseDto> res = especieService.obtenerCantidadAnimalesPorEspecie();

        assertThat(res).hasSize(2);
        AnimalesPorEspecieResponseDto r1 = res.stream().filter(r -> r.getEspecie().equals("E1")).findFirst().orElseThrow();
        AnimalesPorEspecieResponseDto r2 = res.stream().filter(r -> r.getEspecie().equals("E2")).findFirst().orElseThrow();

        assertThat(r1.getCantidadAnimales()).isEqualTo(2);
        assertThat(r2.getCantidadAnimales()).isEqualTo(0);
    }

    @Test
    void obtenerCantidadAnimalesPorEspecie_shouldThrow_whenEmpty() {
        when(especieRepository.findAll()).thenReturn(List.of());
        assertThrows(NoDataFoundException.class, () -> especieService.obtenerCantidadAnimalesPorEspecie());
    }
}