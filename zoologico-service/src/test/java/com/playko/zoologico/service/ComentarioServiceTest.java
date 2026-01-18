package com.playko.zoologico.service;

import com.playko.zoologico.client.MessagingClient;
import com.playko.zoologico.client.dto.SendNotification;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComentarioServiceTest {

    @Mock
    private IComentarioRepository comentarioRepository;

    @Mock
    private IAnimalRepository animalRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private MessagingClient messagingClient;

    @InjectMocks
    private ComentarioService comentarioService;

    private Usuario autor;
    private Usuario creadorAnimal;
    private Animal animal;

    @BeforeEach
    void setUp() {
        autor = new Usuario();
        autor.setId(10L);
        autor.setNombre("Autor");
        autor.setEmail("autor@test.com");

        creadorAnimal = new Usuario();
        creadorAnimal.setId(20L);
        creadorAnimal.setNombre("Creador");
        creadorAnimal.setEmail("creador@test.com");

        animal = new Animal();
        animal.setId(100L);
        animal.setNombre("Firulais");
        animal.setCreatedBy(creadorAnimal);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUserEmail(String email) {
        Authentication auth = mock(Authentication.class);
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getUsername()).thenReturn(email);
        when(auth.getPrincipal()).thenReturn(cud);
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }

    /* ===== agregarComentario ===== */

    @Test
    void agregarComentario_shouldThrow_whenPadreNotFoundAtStart() {
        // Arrange
        ComentarioRequestDto dto = ComentarioRequestDto.builder()
                .padreId(999L)
                .animalId(100L)
                .contenido("Hola")
                .build();

        when(comentarioRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ComentarioPadreNotFoundException.class, () -> comentarioService.agregarComentario(dto));

        // Verificar que solo se llamó a findById en comentarioRepository y que NO se consultó animal/usuario
        verify(comentarioRepository).findById(999L);
        verifyNoInteractions(animalRepository, usuarioRepository);

        // Asegura que no hubo otras interacciones con comentarioRepository
        verifyNoMoreInteractions(comentarioRepository);
    }

    @Test
    void agregarComentario_shouldThrow_whenAnimalNotFound() {
        ComentarioRequestDto dto = ComentarioRequestDto.builder()
                .padreId(null)
                .animalId(999L)
                .contenido("Hola")
                .build();

        when(animalRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AnimalNotFoundException.class, () -> comentarioService.agregarComentario(dto));
    }

    @Test
    void agregarComentario_shouldThrow_whenPadreExistsButAnimalMismatch() {
        // padre exists and belongs to another animal
        Comentario padre = new Comentario();
        padre.setId(1L);
        Animal otroAnimal = new Animal();
        otroAnimal.setId(50L);
        padre.setAnimal(otroAnimal);

        ComentarioRequestDto dto = ComentarioRequestDto.builder()
                .padreId(1L)
                .animalId(animal.getId()) // 100
                .contenido("Respuesta")
                .build();

        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(padre));
        when(animalRepository.findById(animal.getId())).thenReturn(Optional.of(animal));
        // set authenticated user and repository for usuario
        setAuthenticatedUserEmail(autor.getEmail());
        when(usuarioRepository.findByEmail(anyString())).thenReturn(autor);

        assertThrows(ComentarioAnimalMismatchException.class, () -> comentarioService.agregarComentario(dto));
    }

    @Test
    void agregarComentario_shouldSendNotification_whenAutorIsNotCreador() {
        // Arrange
        ComentarioRequestDto dto = ComentarioRequestDto.builder()
                .padreId(null)
                .animalId(animal.getId())
                .contenido("  Contenido nuevo  ")
                .build();

        // NO stubbing de comentarioRepository.findById(...) — innecesario cuando padreId == null
        when(animalRepository.findById(animal.getId())).thenReturn(Optional.of(animal));

        // user different than creator
        autor.setId(11L);
        setAuthenticatedUserEmail(autor.getEmail());
        when(usuarioRepository.findByEmail(autor.getEmail())).thenReturn(autor);

        // Act
        comentarioService.agregarComentario(dto);

        // Assert saved comentario and notification sent
        ArgumentCaptor<Comentario> captor = ArgumentCaptor.forClass(Comentario.class);
        verify(comentarioRepository).save(captor.capture());
        Comentario saved = captor.getValue();

        assertThat(saved.getContenido()).isEqualTo("Contenido nuevo"); // trimmed
        assertThat(saved.getAnimal()).isEqualTo(animal);
        assertThat(saved.getAutor()).isEqualTo(autor);
        assertThat(saved.getFecha()).isNotNull();

        ArgumentCaptor<SendNotification> notifCaptor = ArgumentCaptor.forClass(SendNotification.class);
        verify(messagingClient).sendNotification(notifCaptor.capture());
        SendNotification sent = notifCaptor.getValue();
        assertThat(sent.getTo()).isEqualTo(creadorAnimal.getEmail());
        assertThat(sent.getSubject()).contains("Nuevo comentario sobre el animal");
    }


    @Test
    void agregarComentario_shouldNotSendNotification_whenAutorIsCreador() {
        ComentarioRequestDto dto = ComentarioRequestDto.builder()
                .padreId(null)
                .animalId(animal.getId())
                .contenido("Hola")
                .build();

        when(animalRepository.findById(animal.getId())).thenReturn(Optional.of(animal));

        // autor is same as creador
        autor.setId(creadorAnimal.getId());
        setAuthenticatedUserEmail(autor.getEmail());
        when(usuarioRepository.findByEmail(autor.getEmail())).thenReturn(autor);

        comentarioService.agregarComentario(dto);

        verify(comentarioRepository).save(any(Comentario.class));
        verify(messagingClient, never()).sendNotification(any());
    }

    @Test
    void agregarComentario_withPadreValid_shouldSetPadreAndSendNotification_ifNeeded() {
        Comentario padre = new Comentario();
        padre.setId(5L);
        padre.setAnimal(animal); // same animal -> OK
        padre.setContenido("Padre contenido");
        padre.setAutor(autor);

        ComentarioRequestDto dto = ComentarioRequestDto.builder()
                .padreId(5L)
                .animalId(animal.getId())
                .contenido(" Respuesta al padre ")
                .build();

        when(comentarioRepository.findById(5L)).thenReturn(Optional.of(padre)); // first initial check
        when(animalRepository.findById(animal.getId())).thenReturn(Optional.of(animal));
        // second findById used again inside branch
        when(comentarioRepository.findById(5L)).thenReturn(Optional.of(padre));

        // autor different than creador => notification
        autor.setId(77L);
        setAuthenticatedUserEmail(autor.getEmail());
        when(usuarioRepository.findByEmail(autor.getEmail())).thenReturn(autor);

        comentarioService.agregarComentario(dto);

        ArgumentCaptor<Comentario> captor = ArgumentCaptor.forClass(Comentario.class);
        verify(comentarioRepository).save(captor.capture());
        Comentario saved = captor.getValue();

        assertThat(saved.getPadre()).isEqualTo(padre);
        assertThat(saved.getContenido()).isEqualTo("Respuesta al padre");

        verify(messagingClient).sendNotification(any(SendNotification.class));
    }

    /* ===== obtenerMuroDeAnimal ===== */

    @Test
    void obtenerMuroDeAnimal_shouldThrow_whenAnimalNotFound() {
        when(animalRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(AnimalNotFoundException.class, () -> comentarioService.obtenerMuroDeAnimal(999L));
    }

    @Test
    void obtenerMuroDeAnimal_shouldThrow_whenNoCommentsExist() {
        when(animalRepository.findById(animal.getId())).thenReturn(Optional.of(animal));
        when(comentarioRepository.findByAnimalAndPadreIsNullOrderByFechaAsc(animal)).thenReturn(List.of());
        when(comentarioRepository.existsByAnimal_Id(animal.getId())).thenReturn(false);

        // Extraer el ID del animal antes de la lambda
        Long animalId = animal.getId();

        assertThrows(AnimalSinComentariosException.class, () -> comentarioService.obtenerMuroDeAnimal(animalId));
    }


    @Test
    void obtenerMuroDeAnimal_shouldReturnList_whenCommentsExist() {
        Comentario c = new Comentario();
        c.setId(111L);
        c.setContenido("Un comentario");
        c.setFecha(LocalDateTime.now().minusHours(1));
        c.setAutor(autor);
        c.setAnimal(animal);
        c.setRespuestas(List.of()); // no child replies

        when(animalRepository.findById(animal.getId())).thenReturn(Optional.of(animal));
        when(comentarioRepository.findByAnimalAndPadreIsNullOrderByFechaAsc(animal)).thenReturn(List.of(c));
        when(comentarioRepository.existsByAnimal_Id(animal.getId())).thenReturn(true);

        List<ComentarioResponseDto> res = comentarioService.obtenerMuroDeAnimal(animal.getId());

        assertThat(res).hasSize(1);
        ComentarioResponseDto dto = res.get(0);
        assertThat(dto.getId()).isEqualTo(111L);
        assertThat(dto.getContenido()).isEqualTo("Un comentario");
        assertThat(dto.getAutorId()).isEqualTo(autor.getId());
        assertThat(dto.getRespuestas()).isEmpty();
    }

    /* ===== obtenerPorcentajeComentariosConRespuestas ===== */

    @Test
    void obtenerPorcentajeComentariosConRespuestas_shouldReturnZero_whenNoParents() {
        when(comentarioRepository.findByPadreIsNull()).thenReturn(List.of());
        PorcentajeComentariosConRespuestasDto dto = comentarioService.obtenerPorcentajeComentariosConRespuestas();
        assertThat(dto.getPorcentaje()).isEqualTo("0.0%");
    }

}