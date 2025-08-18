package com.playko.zoologico.service;

import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
import com.playko.zoologico.dto.request.AnimalRequestDto;
import com.playko.zoologico.dto.response.AnimalRegistradoResponseDto;
import com.playko.zoologico.dto.response.AnimalResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.exception.ErrorGettingMailTokenException;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.animal.AnimalesNoEncontradosEnFechaException;
import com.playko.zoologico.exception.especie.EspecieNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IUsuarioRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.impl.AnimalService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalServiceTest {

    @Mock
    private IAnimalRepository animalRepository;

    @Mock
    private IUsuarioRepository usuarioRepository;

    @Mock
    private IEspecieRepository especieRepository;

    @Mock
    private IZonaRepository zonaRepository;

    @InjectMocks
    private AnimalService animalService;

    private Especie especie;
    private Zona zona;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        zona = new Zona();
        zona.setId(10L);
        zona.setNombre("Zona Norte");

        especie = new Especie();
        especie.setId(5L);
        especie.setNombre("Perro");
        especie.setZona(zona);

        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("usuario@test.com");
        usuario.setNombre("Creador");
    }
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void setAuthenticatedUserEmail(String email) {
        Authentication auth = mock(Authentication.class);
        // mockeamos CustomUserDetails para que el instanceof pase
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getUsername()).thenReturn(email);
        when(auth.getPrincipal()).thenReturn(cud);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
    }


    /* ===== obtenerAnimalPorId ===== */

    @Test
    void obtenerAnimalPorId_shouldReturnDto_whenAnimalExists_withComentarios() {
        // preparar animal con comentarios
        Animal a = new Animal();
        a.setId(100L);
        a.setNombre("  Nemo  ");
        a.setFechaIngreso(LocalDateTime.now().minusDays(1));
        a.setEspecie(especie);
        Comentario c1 = new Comentario(); c1.setContenido("Comentario 1");
        Comentario c2 = new Comentario(); c2.setContenido("Comentario 2");
        a.setComentarios(List.of(c1, c2));

        when(animalRepository.findById(100L)).thenReturn(Optional.of(a));

        AnimalResponseDto dto = animalService.obtenerAnimalPorId(100L);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(100L);
        assertThat(dto.getNombre()).isEqualTo("  Nemo  ");
        assertThat(dto.getEspecieId()).isEqualTo(especie.getId());
        assertThat(dto.getComentarios()).containsExactly("Comentario 1", "Comentario 2");
    }

    @Test
    void obtenerAnimalPorId_shouldThrow_whenNotFound() {
        when(animalRepository.findById(200L)).thenReturn(Optional.empty());
        assertThrows(AnimalNotFoundException.class, () -> animalService.obtenerAnimalPorId(200L));
    }

    /* ===== obtenerTodosLosAnimales ===== */

    @Test
    void obtenerTodosLosAnimales_shouldReturnList_whenNotEmpty() {
        Animal a1 = new Animal();
        a1.setId(1L);
        a1.setNombre("A1");
        a1.setFechaIngreso(LocalDateTime.now());
        a1.setEspecie(especie);
        a1.setComentarios(null); // provee la rama donde comentarios == null

        Animal a2 = new Animal();
        a2.setId(2L);
        a2.setNombre("A2");
        a2.setFechaIngreso(LocalDateTime.now());
        a2.setEspecie(especie);
        Comentario c = new Comentario(); c.setContenido("Hola");
        a2.setComentarios(List.of(c));

        when(animalRepository.findAll()).thenReturn(List.of(a1, a2));

        List<AnimalResponseDto> resultado = animalService.obtenerTodosLosAnimales();

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getComentarios()).isEmpty(); // rama comentarios == null -> lista vacía
        assertThat(resultado.get(1).getComentarios()).containsExactly("Hola");
    }

    @Test
    void obtenerTodosLosAnimales_shouldThrow_whenEmpty() {
        when(animalRepository.findAll()).thenReturn(List.of());
        assertThrows(NoDataFoundException.class, () -> animalService.obtenerTodosLosAnimales());
    }

    /* ===== crearAnimal ===== */

    @Test
    void crearAnimal_shouldSaveAnimal_withFechaProvided() {
        // Arrange
        AnimalRequestDto dto = AnimalRequestDto.builder()
                .nombre("  Firulais  ")
                .especieId(especie.getId())
                .fechaIngreso(LocalDateTime.of(2023,1,2,3,4))
                .build();

        // preparar el SecurityContext para que obtenerCorreoDelToken() funcione
        Authentication auth = mock(Authentication.class);
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getUsername()).thenReturn(usuario.getEmail()); // devuelve "usuario@test.com"
        when(auth.getPrincipal()).thenReturn(cud);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        // repositorios
        when(especieRepository.findById(especie.getId())).thenReturn(Optional.of(especie));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(usuario);

        // Act
        animalService.crearAnimal(dto);

        // Assert
        ArgumentCaptor<Animal> captor = ArgumentCaptor.forClass(Animal.class);
        verify(animalRepository).save(captor.capture());
        Animal saved = captor.getValue();

        assertThat(saved.getNombre()).isEqualTo("Firulais"); // .trim()
        assertThat(saved.getEspecie()).isEqualTo(especie);
        assertThat(saved.getCreador()).isEqualTo(usuario);
        assertThat(saved.getFechaIngreso()).isEqualTo(dto.getFechaIngreso());
    }

    @Test
    void crearAnimal_shouldSaveAnimal_whenFechaNull_setsNow() {
        // Arrange
        AnimalRequestDto dto = AnimalRequestDto.builder()
                .nombre("  SinFecha  ")
                .especieId(especie.getId())
                .fechaIngreso(null)
                .build();

        when(especieRepository.findById(especie.getId())).thenReturn(Optional.of(especie));
        when(usuarioRepository.findByEmail(anyString())).thenReturn(usuario);

        // preparar SecurityContext para que obtenerCorreoDelToken() devuelva usuario.getEmail()
        setAuthenticatedUserEmail(usuario.getEmail());

        // Act
        animalService.crearAnimal(dto);

        // Assert
        ArgumentCaptor<Animal> captor = ArgumentCaptor.forClass(Animal.class);
        verify(animalRepository).save(captor.capture());
        Animal saved = captor.getValue();

        assertThat(saved.getNombre()).isEqualTo("SinFecha"); // .trim()
        assertThat(saved.getFechaIngreso()).isNotNull();
        // fecha se coloca en now; comprobamos que esté en un rango razonable (antes de ahora + 2s)
        assertThat(saved.getFechaIngreso()).isBeforeOrEqualTo(LocalDateTime.now().plusSeconds(2));
    }

    @Test
    void crearAnimal_shouldThrow_whenEspecieNotFound() {
        AnimalRequestDto dto = AnimalRequestDto.builder()
                .nombre("X")
                .especieId(999L)
                .build();

        when(especieRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(EspecieNotFoundException.class, () -> animalService.crearAnimal(dto));
        verify(animalRepository, never()).save(any());
    }

    /* ===== editarAnimal ===== */

    @Test
    void editarAnimal_shouldUpdateAndSave_whenAllGood() {
        Long id = 11L;
        Animal existing = new Animal();
        existing.setId(id);
        existing.setNombre("Old");
        existing.setEspecie(especie);

        Especie nueva = new Especie();
        nueva.setId(77L);
        nueva.setNombre("Gato");

        AnimalRequestDto dto = AnimalRequestDto.builder()
                .nombre("  NuevoNombre  ")
                .especieId(nueva.getId())
                .build();

        when(animalRepository.findById(id)).thenReturn(Optional.of(existing));
        when(especieRepository.findById(nueva.getId())).thenReturn(Optional.of(nueva));

        animalService.editarAnimal(id, dto);

        ArgumentCaptor<Animal> captor = ArgumentCaptor.forClass(Animal.class);
        verify(animalRepository).save(captor.capture());
        Animal saved = captor.getValue();

        assertThat(saved.getNombre()).isEqualTo("NuevoNombre");
        assertThat(saved.getEspecie()).isEqualTo(nueva);
    }

    @Test
    void editarAnimal_shouldThrow_whenAnimalNotFound() {
        when(animalRepository.findById(999L)).thenReturn(Optional.empty());
        AnimalRequestDto dto = AnimalRequestDto.builder().nombre("X").especieId(1L).build();
        assertThrows(AnimalNotFoundException.class, () -> animalService.editarAnimal(999L, dto));
    }

    @Test
    void editarAnimal_shouldThrow_whenEspecieNotFound() {
        Long id = 12L;
        Animal existing = new Animal();
        existing.setId(id);
        existing.setNombre("Old");

        when(animalRepository.findById(id)).thenReturn(Optional.of(existing));
        when(especieRepository.findById(55L)).thenReturn(Optional.empty());

        AnimalRequestDto dto = AnimalRequestDto.builder().nombre("X").especieId(55L).build();
        assertThrows(EspecieNotFoundException.class, () -> animalService.editarAnimal(id, dto));
        verify(animalRepository, never()).save(any());
    }

    /* ===== eliminarAnimal ===== */

    @Test
    void eliminarAnimal_shouldDelete_whenExists() {
        Long id = 20L;
        Animal existing = new Animal();
        existing.setId(id);
        when(animalRepository.findById(id)).thenReturn(Optional.of(existing));

        animalService.eliminarAnimal(id);

        verify(animalRepository).delete(existing);
    }

    @Test
    void eliminarAnimal_shouldThrow_whenNotFound() {
        when(animalRepository.findById(33L)).thenReturn(Optional.empty());
        assertThrows(AnimalNotFoundException.class, () -> animalService.eliminarAnimal(33L));
    }

    /* ===== obtenerAnimalesRegistradosEnFecha ===== */

    @Test
    void obtenerAnimalesRegistradosEnFecha_shouldReturnMappedList_whenFound() {
        LocalDate fecha = LocalDate.of(2024, 4, 1);
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        Animal a = new Animal();
        a.setNombre("Bobby");
        a.setFechaIngreso(inicio.plusHours(5));
        Especie e = new Especie();
        e.setNombre("Pez");
        Zona z = new Zona();
        z.setNombre("Zona Oeste");
        e.setZona(z);
        a.setEspecie(e);

        when(animalRepository.findByFechaIngresoBetween(inicio, fin)).thenReturn(List.of(a));

        List<AnimalRegistradoResponseDto> res = animalService.obtenerAnimalesRegistradosEnFecha(fecha);

        assertThat(res).hasSize(1);
        AnimalRegistradoResponseDto dto = res.get(0);
        assertThat(dto.getNombreAnimal()).isEqualTo("Bobby");
        assertThat(dto.getEspecie()).isEqualTo("Pez");
        assertThat(dto.getZona()).isEqualTo("Zona Oeste");
    }

    @Test
    void obtenerAnimalesRegistradosEnFecha_shouldThrow_whenNoAnimals() {
        LocalDate fecha = LocalDate.now().minusDays(5);
        LocalDateTime inicio = fecha.atStartOfDay();
        LocalDateTime fin = fecha.atTime(LocalTime.MAX);

        when(animalRepository.findByFechaIngresoBetween(inicio, fin)).thenReturn(List.of());

        assertThrows(AnimalesNoEncontradosEnFechaException.class,
                () -> animalService.obtenerAnimalesRegistradosEnFecha(fecha));
    }

    /* ===== obtenerCorreoDelToken ===== */

    @Test
    void obtenerCorreoDelToken_shouldReturnEmail_whenAuthHasCustomUserDetails() {
        Authentication auth = mock(Authentication.class);
        CustomUserDetails cud = mock(CustomUserDetails.class);
        when(cud.getUsername()).thenReturn("correo@user.com");
        when(auth.getPrincipal()).thenReturn(cud);

        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        String email = animalService.obtenerCorreoDelToken();
        assertThat(email).isEqualTo("correo@user.com");
    }

    @Test
    void obtenerCorreoDelToken_shouldThrow_whenNoAuthOrWrongPrincipal() {
        // caso 1: auth null
        SecurityContext ctx1 = mock(SecurityContext.class);
        when(ctx1.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(ctx1);
        assertThrows(ErrorGettingMailTokenException.class, () -> animalService.obtenerCorreoDelToken());

        // caso 2: principal no es CustomUserDetails
        Authentication auth2 = mock(Authentication.class);
        when(auth2.getPrincipal()).thenReturn("un string cualquiera");
        SecurityContext ctx2 = mock(SecurityContext.class);
        when(ctx2.getAuthentication()).thenReturn(auth2);
        SecurityContextHolder.setContext(ctx2);
        assertThrows(ErrorGettingMailTokenException.class, () -> animalService.obtenerCorreoDelToken());
    }
}
