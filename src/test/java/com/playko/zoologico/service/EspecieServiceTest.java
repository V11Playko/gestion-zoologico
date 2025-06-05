package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.EspecieRequestDto;
import com.playko.zoologico.dto.response.AnimalesPorEspecieResponseDto;
import com.playko.zoologico.dto.response.EspecieResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.especie.EspecieAlreadyExistsException;
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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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
        // Zona de ejemplo
        zona = new Zona();
        zona.setId(1L);
        zona.setNombre("Zona Test");

        // Especie de ejemplo (sin animales primero)
        especie = new Especie();
        especie.setId(2L);
        especie.setNombre("Especie Test");
        especie.setZona(zona);
        especie.setAnimales(Collections.emptyList());

        // Animal de ejemplo para la prueba de eliminar con animales
        animal = new Animal();
        animal.setId(3L);
        animal.setNombre("Animal Test");
        animal.setEspecie(especie);
    }

    // ====== obtenerEspeciePorId ======

    @Test
    void testObtenerEspeciePorId_Success() {
        when(especieRepository.findById(2L)).thenReturn(Optional.of(especie));

        EspecieResponseDto dto = especieService.obtenerEspeciePorId(2L);

        assertNotNull(dto);
        assertEquals(especie.getId(), dto.getId());
        assertEquals(especie.getNombre(), dto.getNombre());
        assertEquals(zona.getNombre(), dto.getZonaName());
        assertTrue(dto.getNameAnimales().isEmpty());
        verify(especieRepository, times(1)).findById(2L);
    }

    @Test
    void testObtenerEspeciePorId_NotFound() {
        when(especieRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class, () ->
                especieService.obtenerEspeciePorId(99L)
        );
        verify(especieRepository, times(1)).findById(99L);
    }

    // ====== obtenerTodasLasEspecies ======

    @Test
    void testObtenerTodasLasEspecies_Success() {
        Especie otraEspecie = new Especie();
        otraEspecie.setId(4L);
        otraEspecie.setNombre("Otra Especie");
        otraEspecie.setZona(zona);
        otraEspecie.setAnimales(List.of(animal)); // un animal asociado

        when(especieRepository.findAll()).thenReturn(List.of(especie, otraEspecie));

        List<EspecieResponseDto> lista = especieService.obtenerTodasLasEspecies();

        assertEquals(2, lista.size());
        // Verifica que uno de ellos es 'Especie Test' sin animales
        assertTrue(lista.stream().anyMatch(dto ->
                dto.getId().equals(2L) &&
                        dto.getNameAnimales().isEmpty()
        ));
        // Verifica que 'Otra Especie' tiene un nombre en nameAnimales
        assertTrue(lista.stream().anyMatch(dto ->
                dto.getId().equals(4L) &&
                        dto.getNameAnimales().contains("Animal Test")
        ));
        verify(especieRepository, times(1)).findAll();
    }

    @Test
    void testObtenerTodasLasEspecies_NoData() {
        when(especieRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(NoDataFoundException.class, () ->
                especieService.obtenerTodasLasEspecies()
        );
        verify(especieRepository, times(1)).findAll();
    }

    // ====== crearEspecie ======

    @Test
    void testCrearEspecie_Success() {
        EspecieRequestDto request = new EspecieRequestDto();
        request.setNombre("Nueva Especie");
        request.setZonaName(zona.getNombre());

        when(especieRepository.existsByNombreIgnoreCase("Nueva Especie"))
                .thenReturn(false);
        when(zonaRepository.findByNombreIgnoreCase(zona.getNombre()))
                .thenReturn(Optional.of(zona));
        when(especieRepository.save(any(Especie.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> especieService.crearEspecie(request));

        verify(especieRepository, times(1))
                .existsByNombreIgnoreCase("Nueva Especie");
        verify(zonaRepository, times(1))
                .findByNombreIgnoreCase(zona.getNombre());
        verify(especieRepository, times(1)).save(any(Especie.class));
    }

    @Test
    void testCrearEspecie_AlreadyExists() {
        EspecieRequestDto request = new EspecieRequestDto();
        request.setNombre("Especie Test"); // mismo nombre
        request.setZonaName(zona.getNombre());

        when(especieRepository.existsByNombreIgnoreCase("Especie Test"))
                .thenReturn(true);

        assertThrows(EspecieAlreadyExistsException.class, () ->
                especieService.crearEspecie(request)
        );
        verify(especieRepository, times(1))
                .existsByNombreIgnoreCase("Especie Test");
        verify(zonaRepository, never()).findByNombreIgnoreCase(any());
        verify(especieRepository, never()).save(any());
    }

    @Test
    void testCrearEspecie_ZonaNotFound() {
        EspecieRequestDto request = new EspecieRequestDto();
        request.setNombre("Otra Especie");
        request.setZonaName("Zona No Existe");

        when(especieRepository.existsByNombreIgnoreCase("Otra Especie"))
                .thenReturn(false);
        when(zonaRepository.findByNombreIgnoreCase("Zona No Existe"))
                .thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () ->
                especieService.crearEspecie(request)
        );
        verify(especieRepository).existsByNombreIgnoreCase("Otra Especie");
        verify(zonaRepository).findByNombreIgnoreCase("Zona No Existe");
        verify(especieRepository, never()).save(any());
    }

    // ====== editarEspecie ======

    @Test
    void testEditarEspecie_Success_ChangeNameAndZona() {
        EspecieRequestDto request = new EspecieRequestDto();
        request.setNombre("Especie Editada");
        request.setZonaName(zona.getNombre());

        Especie existente = new Especie();
        existente.setId(2L);
        existente.setNombre("Especie Test");
        existente.setZona(zona);
        existente.setAnimales(Collections.emptyList());

        when(especieRepository.findById(2L))
                .thenReturn(Optional.of(existente));
        // Queremos cambiar el nombre a uno distinto; comprobamos que no existe
        when(especieRepository.existsByNombreIgnoreCase("Especie Editada"))
                .thenReturn(false);
        when(zonaRepository.findByNombreIgnoreCase(zona.getNombre()))
                .thenReturn(Optional.of(zona));
        when(especieRepository.save(any(Especie.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> especieService.editarEspecie(2L, request));

        verify(especieRepository, times(1)).findById(2L);
        verify(especieRepository, times(1)).existsByNombreIgnoreCase("Especie Editada");
        verify(zonaRepository, times(1)).findByNombreIgnoreCase(zona.getNombre());
        verify(especieRepository, times(1)).save(any(Especie.class));
    }

    @Test
    void testEditarEspecie_NotFound() {
        EspecieRequestDto request = new EspecieRequestDto();
        request.setNombre("Especie Nueva");
        request.setZonaName(zona.getNombre());

        when(especieRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class, () ->
                especieService.editarEspecie(99L, request)
        );
        verify(especieRepository, times(1)).findById(99L);
        verify(especieRepository, never()).existsByNombreIgnoreCase(any());
        verify(especieRepository, never()).save(any());
    }

    @Test
    void testEditarEspecie_AlreadyExists_NombreInUse() {
        EspecieRequestDto request = new EspecieRequestDto();
        request.setNombre("Especie Duplicada");
        request.setZonaName(zona.getNombre());

        Especie existente = new Especie();
        existente.setId(2L);
        existente.setNombre("Especie Test");
        existente.setZona(zona);

        when(especieRepository.findById(2L))
                .thenReturn(Optional.of(existente));
        // Simulamos que el nuevo nombre ya existe en otra entidad
        when(especieRepository.existsByNombreIgnoreCase("Especie Duplicada"))
                .thenReturn(true);

        assertThrows(EspecieAlreadyExistsException.class, () ->
                especieService.editarEspecie(2L, request)
        );
        verify(especieRepository, times(1)).findById(2L);
        verify(especieRepository, times(1)).existsByNombreIgnoreCase("Especie Duplicada");
        verify(especieRepository, never()).save(any());
    }

    @Test
    void testEditarEspecie_ZonaNotFound() {
        EspecieRequestDto request = new EspecieRequestDto();
        request.setNombre("Especie Test"); // mismo nombre actual
        request.setZonaName("Zona No Existe");

        Especie existente = new Especie();
        existente.setId(2L);
        existente.setNombre("Especie Test");
        existente.setZona(zona);

        when(especieRepository.findById(2L))
                .thenReturn(Optional.of(existente));
        // Como el nombre no cambia, no chequea existsByNombre..., pero sí busca la zona
        when(zonaRepository.findByNombreIgnoreCase("Zona No Existe"))
                .thenReturn(Optional.empty());

        assertThrows(ZonaNotFoundException.class, () ->
                especieService.editarEspecie(2L, request)
        );
        verify(especieRepository, times(1)).findById(2L);
        verify(zonaRepository, times(1)).findByNombreIgnoreCase("Zona No Existe");
        verify(especieRepository, never()).save(any());
    }

    // ====== eliminarEspecie ======

    @Test
    void testEliminarEspecie_Success() {
        Especie existente = new Especie();
        existente.setId(2L);
        existente.setNombre("Especie Test");
        existente.setZona(zona);
        existente.setAnimales(Collections.emptyList());

        when(especieRepository.findById(2L))
                .thenReturn(Optional.of(existente));
        // Simulamos que no hay animales asociados
        when(animalRepository.existsByEspecie(existente))
                .thenReturn(false);
        doNothing().when(especieRepository).delete(existente);

        assertDoesNotThrow(() -> especieService.eliminarEspecie(2L));

        verify(especieRepository, times(1)).findById(2L);
        verify(animalRepository, times(1)).existsByEspecie(existente);
        verify(especieRepository, times(1)).delete(existente);
    }

    @Test
    void testEliminarEspecie_NotFound() {
        when(especieRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(EspecieNotFoundException.class, () ->
                especieService.eliminarEspecie(99L)
        );
        verify(especieRepository, times(1)).findById(99L);
        verify(animalRepository, never()).existsByEspecie(any());
        verify(especieRepository, never()).delete(any());
    }

    @Test
    void testEliminarEspecie_HasAnimals() {
        Especie existente = new Especie();
        existente.setId(2L);
        existente.setNombre("Especie Test");
        existente.setZona(zona);
        existente.setAnimales(List.of(animal));

        when(especieRepository.findById(2L))
                .thenReturn(Optional.of(existente));
        // Simulamos que sí hay animales asociados
        when(animalRepository.existsByEspecie(existente))
                .thenReturn(true);

        assertThrows(EspecieConAnimalesException.class, () ->
                especieService.eliminarEspecie(2L)
        );
        verify(especieRepository, times(1)).findById(2L);
        verify(animalRepository, times(1)).existsByEspecie(existente);
        verify(especieRepository, never()).delete(any());
    }

    // ====== obtenerCantidadAnimalesPorEspecie ======

    @Test
    void testObtenerCantidadAnimalesPorEspecie_Success() {
        Especie conAnimales = new Especie();
        conAnimales.setId(5L);
        conAnimales.setNombre("Especie Con Animales");
        conAnimales.setZona(zona);

        Animal a1 = new Animal();
        a1.setId(6L);
        a1.setNombre("A1");
        a1.setEspecie(conAnimales);
        Animal a2 = new Animal();
        a2.setId(7L);
        a2.setNombre("A2");
        a2.setEspecie(conAnimales);
        conAnimales.setAnimales(List.of(a1, a2));

        when(especieRepository.findAll())
                .thenReturn(List.of(especie, conAnimales));

        List<AnimalesPorEspecieResponseDto> list =
                especieService.obtenerCantidadAnimalesPorEspecie();

        assertEquals(2, list.size());
        // Uno de ellos (especie sin animales) debería tener cantidad 0
        assertTrue(list.stream().anyMatch(dto ->
                dto.getEspecie().equals("Especie Test") && dto.getCantidadAnimales() == 0
        ));
        // El otro (con 2 animales) debería tener cantidad 2
        assertTrue(list.stream().anyMatch(dto ->
                dto.getEspecie().equals("Especie Con Animales") && dto.getCantidadAnimales() == 2
        ));
        verify(especieRepository, times(1)).findAll();
    }

    @Test
    void testObtenerCantidadAnimalesPorEspecie_NoData() {
        when(especieRepository.findAll())
                .thenReturn(Collections.emptyList());

        assertThrows(NoDataFoundException.class, () ->
                especieService.obtenerCantidadAnimalesPorEspecie()
        );
        verify(especieRepository, times(1)).findAll();
    }
}
