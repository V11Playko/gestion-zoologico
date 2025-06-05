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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    private Zona zonaMatch;
    private Especie especieMatch;
    private Animal animalMatch;
    private Comentario comentarioPadre;
    private Comentario comentarioRespuesta;

    @BeforeEach
    void setUp() {
        // Zona que coincida con la búsqueda
        zonaMatch = new Zona();
        zonaMatch.setId(1L);
        zonaMatch.setNombre("ZonaTest");

        // Especie que coincida con la búsqueda, perteneciente a zonaMatch
        especieMatch = new Especie();
        especieMatch.setId(2L);
        especieMatch.setNombre("EspecieTest");
        especieMatch.setZona(zonaMatch);

        // Animal que coincida con la búsqueda, perteneciente a especieMatch
        animalMatch = new Animal();
        animalMatch.setId(3L);
        animalMatch.setNombre("AnimalTest");
        animalMatch.setEspecie(especieMatch);

        // Comentario padre que coincida con la búsqueda
        comentarioPadre = new Comentario();
        comentarioPadre.setId(4L);
        comentarioPadre.setContenido("Este es un comentarioTest");
        comentarioPadre.setAnimal(animalMatch);
        comentarioPadre.setAutor(null);
        comentarioPadre.setPadre(null);

        // Comentario respuesta que coincida con la búsqueda
        comentarioRespuesta = new Comentario();
        comentarioRespuesta.setId(5L);
        comentarioRespuesta.setContenido("Esta es una respuestaTest");
        comentarioRespuesta.setAnimal(animalMatch);
        comentarioRespuesta.setAutor(null);
        comentarioRespuesta.setPadre(comentarioPadre);

        // Asociar la respuesta al padre
        comentarioPadre.setRespuestas(List.of(comentarioRespuesta));
    }

    @Test
    void testBuscarPorPalabra_EmptyAll() {
        when(zonaRepository.findAll()).thenReturn(Collections.emptyList());
        when(especieRepository.findAll()).thenReturn(Collections.emptyList());
        when(animalRepository.findAll()).thenReturn(Collections.emptyList());
        when(comentarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<BusquedaResultadoDto> resultados = busquedaService.buscarPorPalabra("xyz");
        assertTrue(resultados.isEmpty());

        verify(zonaRepository, times(1)).findAll();
        verify(especieRepository, times(1)).findAll();
        verify(animalRepository, times(1)).findAll();
        verify(comentarioRepository, times(1)).findAll();
    }

    @Test
    void testBuscarPorPalabra_ZonaMatch() {
        when(zonaRepository.findAll()).thenReturn(List.of(zonaMatch));
        when(especieRepository.findAll()).thenReturn(Collections.emptyList());
        when(animalRepository.findAll()).thenReturn(Collections.emptyList());
        when(comentarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<BusquedaResultadoDto> resultados = busquedaService.buscarPorPalabra("ZonaTest");
        assertEquals(1, resultados.size());
        BusquedaResultadoDto dto = resultados.get(0);
        assertEquals("ZONA", dto.getTipoResultado());
        assertEquals("ZonaTest", dto.getZonaNombre());
        assertNull(dto.getEspecieNombre());
        assertNull(dto.getAnimalNombre());

        verify(zonaRepository).findAll();
    }

    @Test
    void testBuscarPorPalabra_EspecieMatch() {
        when(zonaRepository.findAll()).thenReturn(Collections.emptyList());
        when(especieRepository.findAll()).thenReturn(List.of(especieMatch));
        when(animalRepository.findAll()).thenReturn(Collections.emptyList());
        when(comentarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<BusquedaResultadoDto> resultados = busquedaService.buscarPorPalabra("EspecieTest");
        assertEquals(1, resultados.size());
        BusquedaResultadoDto dto = resultados.get(0);
        assertEquals("ESPECIE", dto.getTipoResultado());
        assertEquals("ZonaTest", dto.getZonaNombre());
        assertEquals("EspecieTest", dto.getEspecieNombre());
        assertNull(dto.getAnimalNombre());

        verify(especieRepository).findAll();
    }

    @Test
    void testBuscarPorPalabra_AnimalMatch() {
        when(zonaRepository.findAll()).thenReturn(Collections.emptyList());
        when(especieRepository.findAll()).thenReturn(Collections.emptyList());
        when(animalRepository.findAll()).thenReturn(List.of(animalMatch));
        when(comentarioRepository.findAll()).thenReturn(Collections.emptyList());

        List<BusquedaResultadoDto> resultados = busquedaService.buscarPorPalabra("AnimalTest");
        assertEquals(1, resultados.size());
        BusquedaResultadoDto dto = resultados.get(0);
        assertEquals("ANIMAL", dto.getTipoResultado());
        assertEquals("ZonaTest", dto.getZonaNombre());
        assertEquals("EspecieTest", dto.getEspecieNombre());
        assertEquals("AnimalTest", dto.getAnimalNombre());

        verify(animalRepository).findAll();
    }

    @Test
    void testBuscarPorPalabra_ComentarioPadreMatch() {
        when(zonaRepository.findAll()).thenReturn(Collections.emptyList());
        when(especieRepository.findAll()).thenReturn(Collections.emptyList());
        when(animalRepository.findAll()).thenReturn(Collections.emptyList());
        when(comentarioRepository.findAll()).thenReturn(List.of(comentarioPadre, comentarioRespuesta));

        List<BusquedaResultadoDto> resultados = busquedaService.buscarPorPalabra("comentarioTest");
        assertEquals(1, resultados.size());
        BusquedaResultadoDto dto = resultados.get(0);
        assertEquals("COMENTARIO", dto.getTipoResultado());
        assertEquals("ZonaTest", dto.getZonaNombre());
        assertEquals("EspecieTest", dto.getEspecieNombre());
        assertEquals("AnimalTest", dto.getAnimalNombre());
        assertEquals("Este es un comentarioTest", dto.getComentarioContenido());
        assertNull(dto.getRespuestaContenido());

        verify(comentarioRepository).findAll();
    }

    @Test
    void testBuscarPorPalabra_RespuestaMatch() {
        when(zonaRepository.findAll()).thenReturn(Collections.emptyList());
        when(especieRepository.findAll()).thenReturn(Collections.emptyList());
        when(animalRepository.findAll()).thenReturn(Collections.emptyList());
        when(comentarioRepository.findAll()).thenReturn(List.of(comentarioPadre, comentarioRespuesta));

        List<BusquedaResultadoDto> resultados = busquedaService.buscarPorPalabra("respuestaTest");
        assertEquals(1, resultados.size());
        BusquedaResultadoDto dto = resultados.get(0);
        assertEquals("RESPUESTA", dto.getTipoResultado());
        assertEquals("ZonaTest", dto.getZonaNombre());
        assertEquals("EspecieTest", dto.getEspecieNombre());
        assertEquals("AnimalTest", dto.getAnimalNombre());
        assertEquals("Este es un comentarioTest", dto.getComentarioContenido());
        assertEquals("Esta es una respuestaTest", dto.getRespuestaContenido());

        verify(comentarioRepository).findAll();
    }
}