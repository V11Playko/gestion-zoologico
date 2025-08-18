package com.playko.zoologico.service;

import com.playko.zoologico.dto.response.BusquedaResultadoDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IComentarioRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.impl.BusquedaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BusquedaServiceTest {

    @Mock
    private IZonaRepository zonaRepository;

    @Mock
    private IEspecieRepository especieRepository;

    @Mock
    private IAnimalRepository animalRepository;

    @Mock
    private IComentarioRepository comentarioRepository;

    @InjectMocks
    private BusquedaService busquedaService;

    private Zona zona1;
    private Zona zona2;
    private Especie especie1;
    private Especie especie2;
    private Animal animal1;
    private Animal animal2;

    @BeforeEach
    void setUp() {
        zona1 = new Zona();
        zona1.setId(1L);
        zona1.setNombre("Zona Norte");

        zona2 = new Zona();
        zona2.setId(2L);
        zona2.setNombre("Zona Sur");

        especie1 = new Especie();
        especie1.setId(10L);
        especie1.setNombre("Perro");
        especie1.setZona(zona1);

        especie2 = new Especie();
        especie2.setId(11L);
        especie2.setNombre("Gato");
        especie2.setZona(zona2);

        animal1 = new Animal();
        animal1.setId(100L);
        animal1.setNombre("Firulais");
        animal1.setEspecie(especie1);

        animal2 = new Animal();
        animal2.setId(101L);
        animal2.setNombre("Misu");
        animal2.setEspecie(especie2);
    }

    @Test
    void buscarPorPalabra_shouldReturnEmpty_whenAllRepositoriesEmpty() {
        // Arrange
        when(zonaRepository.findAll()).thenReturn(List.of());
        when(especieRepository.findAll()).thenReturn(List.of());
        when(animalRepository.findAll()).thenReturn(List.of());
        when(comentarioRepository.findAll()).thenReturn(List.of());

        // Act
        List<BusquedaResultadoDto> res = busquedaService.buscarPorPalabra("cualquier");

        // Assert
        assertThat(res).isEmpty();
    }

    @Test
    void buscarPorPalabra_shouldMatchZona_caseInsensitive() {
        // Arrange: zona1 has "Zona Norte" -> search "norte" in different case
        when(zonaRepository.findAll()).thenReturn(List.of(zona1, zona2));
        when(especieRepository.findAll()).thenReturn(List.of());
        when(animalRepository.findAll()).thenReturn(List.of());
        when(comentarioRepository.findAll()).thenReturn(List.of());

        // Act
        List<BusquedaResultadoDto> res = busquedaService.buscarPorPalabra("NoRTe");

        // Assert
        assertThat(res).hasSize(1);
        BusquedaResultadoDto dto = res.get(0);
        assertThat(dto.getTipoResultado()).isEqualTo("ZONA");
        assertThat(dto.getZonaNombre()).isEqualTo(zona1.getNombre());
        assertThat(dto.getEspecieNombre()).isNull();
        assertThat(dto.getAnimalNombre()).isNull();
        assertThat(dto.getComentarioContenido()).isNull();
        assertThat(dto.getRespuestaContenido()).isNull();
    }

    @Test
    void buscarPorPalabra_shouldMatchEspecie_andReturnZonaName() {
        // Arrange: especie1 "Perro" belongs to zona1
        when(zonaRepository.findAll()).thenReturn(List.of());
        when(especieRepository.findAll()).thenReturn(List.of(especie1, especie2));
        when(animalRepository.findAll()).thenReturn(List.of());
        when(comentarioRepository.findAll()).thenReturn(List.of());

        // Act
        List<BusquedaResultadoDto> res = busquedaService.buscarPorPalabra("perro");

        // Assert
        assertThat(res).hasSize(1);
        BusquedaResultadoDto dto = res.get(0);
        assertThat(dto.getTipoResultado()).isEqualTo("ESPECIE");
        assertThat(dto.getZonaNombre()).isEqualTo(especie1.getZona().getNombre());
        assertThat(dto.getEspecieNombre()).isEqualTo(especie1.getNombre());
        assertThat(dto.getAnimalNombre()).isNull();
    }

    @Test
    void buscarPorPalabra_shouldMatchAnimal_andReturnEspecieAndZona() {
        // Arrange: animal1 nombre "Firulais"
        when(zonaRepository.findAll()).thenReturn(List.of());
        when(especieRepository.findAll()).thenReturn(List.of());
        when(animalRepository.findAll()).thenReturn(List.of(animal1, animal2));
        when(comentarioRepository.findAll()).thenReturn(List.of());

        // Act
        List<BusquedaResultadoDto> res = busquedaService.buscarPorPalabra("firu");

        // Assert
        assertThat(res).hasSize(1);
        BusquedaResultadoDto dto = res.get(0);
        assertThat(dto.getTipoResultado()).isEqualTo("ANIMAL");
        assertThat(dto.getZonaNombre()).isEqualTo(animal1.getEspecie().getZona().getNombre());
        assertThat(dto.getEspecieNombre()).isEqualTo(animal1.getEspecie().getNombre());
        assertThat(dto.getAnimalNombre()).isEqualTo(animal1.getNombre());
    }

    @Test
    void buscarPorPalabra_shouldMatchComentarioPadre_whenComentarioContenidoMatches() {
        // Arrange: create parent comment with contenido that contains "hola"
        Comentario padre = new Comentario();
        padre.setId(1000L);
        padre.setContenido("Hola Mundo");
        padre.setPadre(null);
        padre.setAnimal(animal1); // animal1 -> especie1 -> zona1

        when(zonaRepository.findAll()).thenReturn(List.of());
        when(especieRepository.findAll()).thenReturn(List.of());
        when(animalRepository.findAll()).thenReturn(List.of());
        when(comentarioRepository.findAll()).thenReturn(List.of(padre));

        // Act
        List<BusquedaResultadoDto> res = busquedaService.buscarPorPalabra("hola");

        // Assert
        assertThat(res).hasSize(1);
        BusquedaResultadoDto dto = res.get(0);
        assertThat(dto.getTipoResultado()).isEqualTo("COMENTARIO");
        assertThat(dto.getZonaNombre()).isEqualTo(animal1.getEspecie().getZona().getNombre());
        assertThat(dto.getEspecieNombre()).isEqualTo(animal1.getEspecie().getNombre());
        assertThat(dto.getAnimalNombre()).isEqualTo(animal1.getNombre());
        assertThat(dto.getComentarioContenido()).isEqualTo(padre.getContenido());
        assertThat(dto.getRespuestaContenido()).isNull();
    }

    @Test
    void buscarPorPalabra_shouldMatchRespuesta_whenChildContenidoMatches() {
        // Arrange: create parent and child
        Comentario padre = new Comentario();
        padre.setId(2000L);
        padre.setContenido("Contenido padre");
        padre.setPadre(null);
        padre.setAnimal(animal2); // animal2 -> especie2 -> zona2

        Comentario respuesta = new Comentario();
        respuesta.setId(2001L);
        respuesta.setContenido("Esta es la respuesta que contiene la palabra busqueda");
        respuesta.setPadre(padre);
        respuesta.setAnimal(animal2);

        when(zonaRepository.findAll()).thenReturn(List.of());
        when(especieRepository.findAll()).thenReturn(List.of());
        when(animalRepository.findAll()).thenReturn(List.of());
        when(comentarioRepository.findAll()).thenReturn(List.of(respuesta)); // only respuesta returned

        // Act
        List<BusquedaResultadoDto> res = busquedaService.buscarPorPalabra("Busqueda");

        // Assert
        assertThat(res).hasSize(1);
        BusquedaResultadoDto dto = res.get(0);
        assertThat(dto.getTipoResultado()).isEqualTo("RESPUESTA");
        assertThat(dto.getZonaNombre()).isEqualTo(padre.getAnimal().getEspecie().getZona().getNombre());
        assertThat(dto.getEspecieNombre()).isEqualTo(padre.getAnimal().getEspecie().getNombre());
        assertThat(dto.getAnimalNombre()).isEqualTo(padre.getAnimal().getNombre());
        assertThat(dto.getComentarioContenido()).isEqualTo(padre.getContenido());
        assertThat(dto.getRespuestaContenido()).isEqualTo(respuesta.getContenido());
    }

    @Test
    void buscarPorPalabra_shouldReturnAllMatches_inExpectedOrder() {
        // Arrange: zona match, especie match, animal match, respuesta match
        Zona zonaMatch = new Zona();
        zonaMatch.setId(99L);
        zonaMatch.setNombre("Zona Palabra"); // contiene "palabra"

        Especie especieMatch = new Especie();
        especieMatch.setId(199L);
        especieMatch.setNombre("EspeciePalabra"); // contiene "palabra"
        especieMatch.setZona(zonaMatch);

        Animal animalMatch = new Animal();
        animalMatch.setId(299L);
        animalMatch.setNombre("Nombre Palabra"); // contiene "palabra"
        animalMatch.setEspecie(especieMatch);

        Comentario padre = new Comentario();
        padre.setId(3000L);
        padre.setContenido("Padre que no tiene la palabra"); // no coincide
        padre.setPadre(null);
        padre.setAnimal(animalMatch);

        Comentario respuesta = new Comentario();
        respuesta.setId(3001L);
        respuesta.setContenido("respuesta con palabra clave"); // contiene "palabra"
        respuesta.setPadre(padre);
        respuesta.setAnimal(animalMatch);

        when(zonaRepository.findAll()).thenReturn(List.of(zonaMatch));
        when(especieRepository.findAll()).thenReturn(List.of(especieMatch));
        when(animalRepository.findAll()).thenReturn(List.of(animalMatch));
        when(comentarioRepository.findAll()).thenReturn(List.of(respuesta)); // sólo respuesta contiene la palabra

        // Act
        List<BusquedaResultadoDto> res = busquedaService.buscarPorPalabra("palabra");

        // Assert: order is ZONA, ESPECIE, ANIMAL, then RESPUESTA
        assertThat(res).hasSize(4);
        assertThat(res.get(0).getTipoResultado()).isEqualTo("ZONA");
        assertThat(res.get(1).getTipoResultado()).isEqualTo("ESPECIE");
        assertThat(res.get(2).getTipoResultado()).isEqualTo("ANIMAL");
        assertThat(res.get(3).getTipoResultado()).isEqualTo("RESPUESTA");
    }

}