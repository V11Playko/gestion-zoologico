package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.ComentarioRequestDto;
import com.playko.zoologico.dto.response.ComentarioResponseDto;
import com.playko.zoologico.dto.response.PorcentajeComentariosConRespuestasDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.animal.AnimalSinComentariosException;
import com.playko.zoologico.exception.comentario.ComentarioAnimalMismatchException;
import com.playko.zoologico.exception.comentario.ComentarioPadreNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IComentarioRepository;
import com.playko.zoologico.repository.IUsuarioRepository;
import com.playko.zoologico.service.impl.ComentarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class ComentarioServiceTest {

    @Mock
    private IComentarioRepository comentarioRepository;

    @Mock
    private IAnimalRepository animalRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks @Spy
    private ComentarioService comentarioService;

    private Animal animal;
    private Usuario usuario;
    private Comentario padreComentario;

    @BeforeEach
    void setUp() {
        // Configuramos un Animal de prueba
        animal = new Animal();
        animal.setId(10L);
        animal.setNombre("Animal Test");

        // Configuramos un Usuario de prueba
        usuario = new Usuario();
        usuario.setId(20L);
        usuario.setNombre("Usuario Test");
        usuario.setEmail("test@correo.com");

        // Configuramos un Comentario padre de prueba
        padreComentario = new Comentario();
        padreComentario.setId(30L);
        padreComentario.setContenido("Comentario Padre");
        padreComentario.setFecha(LocalDateTime.now());
        padreComentario.setAnimal(animal);
        padreComentario.setAutor(usuario);
        padreComentario.setRespuestas(new ArrayList<>());
    }

    // ========== Pruebas para agregarComentario ==========

    @Test
    void testAgregarComentario_SinPadre_Success() {
        ComentarioRequestDto dto = new ComentarioRequestDto();
        dto.setContenido("Nuevo comentario");
        dto.setAnimalNombre("Animal Test");
        dto.setPadreId(null);

        // Simulamos que existe el animal
        when(animalRepository.findByNombreIgnoreCase("Animal Test"))
                .thenReturn(Optional.of(animal));
        // Hacemos spy para devolver un correo fijo sin usar SecurityContextHolder
        doReturn("test@correo.com").when(comentarioService).obtenerCorreoDelToken();
        // Simulamos que existe el usuario con ese correo
        when(usuarioRepository.findByEmail("test@correo.com"))
                .thenReturn(usuario);
        // Al guardar, devolvemos la entidad que se intenta guardar
        when(comentarioRepository.save(any(Comentario.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> comentarioService.agregarComentario(dto));

        verify(animalRepository, times(1)).findByNombreIgnoreCase("Animal Test");
        verify(usuarioRepository, times(1)).findByEmail("test@correo.com");
        verify(comentarioRepository, times(1)).save(any(Comentario.class));
    }

    @Test
    void testAgregarComentario_PadreNoExiste() {
        ComentarioRequestDto dto = new ComentarioRequestDto();
        dto.setContenido("Hijo");
        dto.setAnimalNombre("Animal Test");
        dto.setPadreId(999L);

        // Si el padre no existe, debe lanzar ComentarioPadreNotFoundException
        when(comentarioRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ComentarioPadreNotFoundException.class,
                () -> comentarioService.agregarComentario(dto));
        verify(comentarioRepository, times(1)).findById(999L);
        verify(animalRepository, never()).findByNombreIgnoreCase(any());
        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void testAgregarComentario_AnimalNoExiste() {
        ComentarioRequestDto dto = new ComentarioRequestDto();
        dto.setContenido("Comentario");
        dto.setAnimalNombre("NoExiste");
        dto.setPadreId(null);

        // Si el animal no existe, debe lanzar AnimalNotFoundException
        when(animalRepository.findByNombreIgnoreCase("NoExiste"))
                .thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class,
                () -> comentarioService.agregarComentario(dto));
        verify(animalRepository, times(1)).findByNombreIgnoreCase("NoExiste");
        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void testAgregarComentario_PadreDistintoAnimal() {
        ComentarioRequestDto dto = new ComentarioRequestDto();
        dto.setContenido("Hijo");
        dto.setAnimalNombre("Animal Test");
        dto.setPadreId(30L);

        // El padre existe, pero lo reasignamos a otro animal para forzar mismatch
        when(comentarioRepository.findById(30L))
                .thenReturn(Optional.of(padreComentario));
        when(animalRepository.findByNombreIgnoreCase("Animal Test"))
                .thenReturn(Optional.of(animal));
        // Hacemos que el padre pertenezca a un animal distinto
        Animal otroAnimal = new Animal();
        otroAnimal.setId(50L);
        padreComentario.setAnimal(otroAnimal);

        doReturn("test@correo.com").when(comentarioService).obtenerCorreoDelToken();
        when(usuarioRepository.findByEmail("test@correo.com"))
                .thenReturn(usuario);

        assertThrows(ComentarioAnimalMismatchException.class,
                () -> comentarioService.agregarComentario(dto));
        // Se llama a findById del repositorio dos veces:
        //   - una al principio para validar existencia
        //   - otra al validar mismatch
        verify(comentarioRepository, times(2)).findById(30L);
        verify(comentarioRepository, never()).save(any());
    }

    // ========== Pruebas para obtenerMuroDeAnimal ==========

    @Test
    void testObtenerMuroDeAnimal_Success() {
        // Creamos un hijo para el comentario padre
        Comentario hijo = new Comentario();
        hijo.setId(40L);
        hijo.setContenido("Respuesta");
        hijo.setFecha(LocalDateTime.now());
        hijo.setAnimal(animal);
        hijo.setAutor(usuario);
        hijo.setPadre(padreComentario);

        // Asociamos la respuesta al padre
        padreComentario.getRespuestas().add(hijo);

        when(animalRepository.findByNombreIgnoreCase("Animal Test"))
                .thenReturn(Optional.of(animal));
        when(comentarioRepository.findByAnimalAndPadreIsNullOrderByFechaAsc(animal))
                .thenReturn(List.of(padreComentario));
        when(comentarioRepository.existsByAnimal_Id(animal.getId()))
                .thenReturn(true);

        List<ComentarioResponseDto> muro =
                comentarioService.obtenerMuroDeAnimal("Animal Test");

        assertFalse(muro.isEmpty());
        assertEquals(1, muro.size());
        assertEquals(padreComentario.getContenido(), muro.get(0).getContenido());
        // Verificamos que la lista de respuestas en el DTO no esté vacía
        assertEquals(1, muro.get(0).getRespuestas().size());
        assertEquals(hijo.getContenido(),
                muro.get(0).getRespuestas().get(0).getContenido());

        verify(animalRepository, times(1)).findByNombreIgnoreCase("Animal Test");
        verify(comentarioRepository, times(1))
                .findByAnimalAndPadreIsNullOrderByFechaAsc(animal);
        verify(comentarioRepository, times(1)).existsByAnimal_Id(animal.getId());
    }

    @Test
    void testObtenerMuroDeAnimal_AnimalNoExiste() {
        when(animalRepository.findByNombreIgnoreCase("NoExiste"))
                .thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class,
                () -> comentarioService.obtenerMuroDeAnimal("NoExiste"));
        verify(animalRepository, times(1)).findByNombreIgnoreCase("NoExiste");
        verify(comentarioRepository, never()).findByAnimalAndPadreIsNullOrderByFechaAsc(any());
    }

    @Test
    void testObtenerMuroDeAnimal_SinComentarios() {
        when(animalRepository.findByNombreIgnoreCase("Animal Test"))
                .thenReturn(Optional.of(animal));
        // Existe la entidad Animal, pero no hay comentarios asociados
        when(comentarioRepository.existsByAnimal_Id(animal.getId()))
                .thenReturn(false);

        assertThrows(AnimalSinComentariosException.class,
                () -> comentarioService.obtenerMuroDeAnimal("Animal Test"));
        verify(animalRepository, times(1)).findByNombreIgnoreCase("Animal Test");
    }

    // ========== Pruebas para obtenerPorcentajeComentariosConRespuestas ==========

    @Test
    void testObtenerPorcentajeComentariosConRespuestas_NoPadres() {
        when(comentarioRepository.findByPadreIsNull())
                .thenReturn(Collections.emptyList());

        PorcentajeComentariosConRespuestasDto dto =
                comentarioService.obtenerPorcentajeComentariosConRespuestas();
        assertEquals("0.0%", dto.getPorcentaje());
        verify(comentarioRepository, times(1)).findByPadreIsNull();
    }

    @Test
    void testObtenerPorcentajeComentariosConRespuestas_ConYSinRespuestas() {
        // Creamos dos comentarios padre: uno sin respuestas y otro con una respuesta
        Comentario padre1 = new Comentario();
        padre1.setId(50L);
        padre1.setRespuestas(new ArrayList<>());

        Comentario padre2 = new Comentario();
        padre2.setId(51L);
        Comentario respuesta = new Comentario();
        respuesta.setId(52L);
        padre2.setRespuestas(List.of(respuesta));

        when(comentarioRepository.findByPadreIsNull())
                .thenReturn(List.of(padre1, padre2));

        PorcentajeComentariosConRespuestasDto dto =
                comentarioService.obtenerPorcentajeComentariosConRespuestas();
        // De los 2 padres, 1 tiene respuestas → 50.0%
        assertEquals("50,0%", dto.getPorcentaje());
        verify(comentarioRepository, times(1)).findByPadreIsNull();
    }
}
