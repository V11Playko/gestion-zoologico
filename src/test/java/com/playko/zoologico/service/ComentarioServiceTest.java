package com.playko.zoologico.service;

import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock
    private IComentarioRepository comentarioRepository;

    @Mock
    private IAnimalRepository animalRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @InjectMocks
    @Spy // para poder mockear obtenerCorreoDelToken()
    private ComentarioService comentarioService;

    private Animal animal;
    private Usuario usuario;
    private Comentario comentarioPadre;

    @BeforeEach
    void setUp() {
        animal = new Animal();
        animal.setId(1L);

        usuario = new Usuario();
        usuario.setId(5L);
        usuario.setEmail("test@correo.com");

        comentarioPadre = new Comentario();
        comentarioPadre.setId(100L);
        comentarioPadre.setAnimal(animal);
        comentarioPadre.setRespuestas(new ArrayList<>());
    }

    @Test
    void agregarComentario_sinPadre_debeGuardar() {
        ComentarioRequestDto dto = new ComentarioRequestDto(" Hola ", 1L, 5L, null);
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        doReturn("test@correo.com").when(comentarioService).obtenerCorreoDelToken();
        when(usuarioRepository.findByEmail("test@correo.com")).thenReturn(usuario);

        comentarioService.agregarComentario(dto);

        verify(comentarioRepository).save(argThat(c ->
                c.getContenido().equals("Hola") &&
                        c.getAnimal().equals(animal) &&
                        c.getAutor().equals(usuario) &&
                        c.getPadre() == null
        ));
    }

    @Test
    void agregarComentario_conPadre_debeGuardar() {
        ComentarioRequestDto dto = new ComentarioRequestDto("Hola", 1L, 5L, 100L);
        when(comentarioRepository.findById(100L)).thenReturn(Optional.of(comentarioPadre));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        doReturn("test@correo.com").when(comentarioService).obtenerCorreoDelToken();
        when(usuarioRepository.findByEmail("test@correo.com")).thenReturn(usuario);
        when(comentarioRepository.findById(100L)).thenReturn(Optional.of(comentarioPadre));

        comentarioService.agregarComentario(dto);

        verify(comentarioRepository).save(argThat(c ->
                c.getPadre().equals(comentarioPadre) &&
                        c.getAnimal().equals(animal)
        ));
    }

    @Test
    void agregarComentario_conPadreQueNoExisteDebeLanzarExcepcion() {
        ComentarioRequestDto dto = new ComentarioRequestDto("Hola", 1L, 5L, 999L);
        when(comentarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ComentarioPadreNotFoundException.class,
                () -> comentarioService.agregarComentario(dto));
    }

    @Test
    void agregarComentario_conAnimalQueNoExisteDebeLanzarExcepcion() {
        ComentarioRequestDto dto = new ComentarioRequestDto("Hola", 2L, 5L, null);
        when(animalRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class,
                () -> comentarioService.agregarComentario(dto));
    }

    @Test
    void agregarComentario_conPadreDeOtroAnimalDebeLanzarExcepcion() {
        Animal otroAnimal = new Animal();
        otroAnimal.setId(99L);
        Comentario padreOtroAnimal = new Comentario();
        padreOtroAnimal.setId(100L);
        padreOtroAnimal.setAnimal(otroAnimal);

        ComentarioRequestDto dto = new ComentarioRequestDto("Hola", 1L, 5L, 100L);
        when(comentarioRepository.findById(100L)).thenReturn(Optional.of(padreOtroAnimal));
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        doReturn("test@correo.com").when(comentarioService).obtenerCorreoDelToken();
        when(usuarioRepository.findByEmail("test@correo.com")).thenReturn(usuario);
        when(comentarioRepository.findById(100L)).thenReturn(Optional.of(padreOtroAnimal));

        assertThrows(ComentarioAnimalMismatchException.class,
                () -> comentarioService.agregarComentario(dto));
    }

    @Test
    void obtenerMuroDeAnimal_debeRetornarLista() {
        Comentario comentario = new Comentario();
        comentario.setId(1L);
        comentario.setContenido("Texto");
        comentario.setFecha(LocalDateTime.now());
        comentario.setAutor(usuario);
        comentario.setRespuestas(List.of());

        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(comentarioRepository.findByAnimalAndPadreIsNullOrderByFechaAsc(animal))
                .thenReturn(List.of(comentario));
        when(comentarioRepository.existsByAnimal_Id(1L)).thenReturn(true);

        List<ComentarioResponseDto> result = comentarioService.obtenerMuroDeAnimal(1L);

        assertEquals(1, result.size());
        assertEquals("Texto", result.get(0).getContenido());
    }

    @Test
    void obtenerMuroDeAnimal_conAnimalQueNoExisteDebeLanzarExcepcion() {
        when(animalRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class,
                () -> comentarioService.obtenerMuroDeAnimal(1L));
    }

    @Test
    void obtenerMuroDeAnimal_sinComentariosDebeLanzarExcepcion() {
        when(animalRepository.findById(1L)).thenReturn(Optional.of(animal));
        when(comentarioRepository.existsByAnimal_Id(1L)).thenReturn(false);

        assertThrows(AnimalSinComentariosException.class,
                () -> comentarioService.obtenerMuroDeAnimal(1L));
    }

    @Test
    void obtenerPorcentajeComentariosConRespuestas_conListaVaciaDebeRetornarCero() {
        when(comentarioRepository.findByPadreIsNull()).thenReturn(List.of());

        PorcentajeComentariosConRespuestasDto result = comentarioService.obtenerPorcentajeComentariosConRespuestas();

        assertEquals("0.0%", result.getPorcentaje());
    }

    @Test
    void obtenerPorcentajeComentariosConRespuestas_conDatosDebeCalcularBien() {
        Comentario c1 = new Comentario();
        c1.setRespuestas(List.of(new Comentario()));

        Comentario c2 = new Comentario();
        c2.setRespuestas(List.of());

        when(comentarioRepository.findByPadreIsNull()).thenReturn(List.of(c1, c2));

        PorcentajeComentariosConRespuestasDto result = comentarioService.obtenerPorcentajeComentariosConRespuestas();

        assertEquals("50,0%", result.getPorcentaje());
    }

    @Test
    void obtenerCorreoDelToken_debeRetornarUsuario() {
        Authentication auth = mock(Authentication.class);
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUsername()).thenReturn("correo@prueba.com");
        when(auth.getPrincipal()).thenReturn(userDetails);

        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        String result = comentarioService.obtenerCorreoDelToken();

        assertEquals("correo@prueba.com", result);
    }

    @Test
    void obtenerCorreoDelToken_sinAuthDebeLanzarExcepcion() {
        SecurityContext context = mock(SecurityContext.class);
        when(context.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(context);

        assertThrows(RuntimeException.class,
                () -> comentarioService.obtenerCorreoDelToken());
    }
}
