package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.UsuarioRequestDto;
import com.playko.zoologico.dto.response.UsuarioResponseDto;
import com.playko.zoologico.entity.Role;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.usuario.EmailAlreadyExistsException;
import com.playko.zoologico.exception.usuario.RoleNotFoundException;
import com.playko.zoologico.repository.IRoleRepository;
import com.playko.zoologico.repository.IUsuarioRepository;
import com.playko.zoologico.service.impl.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private IUsuarioRepository usuarioRepository;
    @Mock
    private IRoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Role roleEmpleado;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        // Creamos un role de ejemplo
        roleEmpleado = new Role();
        roleEmpleado.setId(1L);
        roleEmpleado.setNombre("ROLE_EMPLEADO");

        // Creamos un usuario de ejemplo
        usuario = new Usuario();
        usuario.setId(2L);
        usuario.setNombre("Juan Pérez");
        usuario.setEmail("juan@correo.com");
        usuario.setPassword("hashedPassword");
        usuario.setRole(roleEmpleado);
    }

    // ====== crearUsuarioEmpleado ======

    @Test
    void testCrearUsuarioEmpleado_Success() {
        UsuarioRequestDto request = new UsuarioRequestDto();
        request.setNombre("Ana Gómez");
        request.setEmail("ana@correo.com");
        request.setPassword("secret123");

        // Simulamos que no existe el email
        when(usuarioRepository.existsByEmailIgnoreCase("ana@correo.com"))
                .thenReturn(false);
        // Simulamos encontrar el role
        when(roleRepository.findByNombreIgnoreCase("ROLE_EMPLEADO"))
                .thenReturn(Optional.of(roleEmpleado));
        // Simulamos que el passwordEncoder devuelve un hash arbitrario
        when(passwordEncoder.encode("secret123"))
                .thenReturn("hashedSecret");

        // No debería lanzar excepción
        assertDoesNotThrow(() -> usuarioService.crearUsuarioEmpleado(request));

        // Verificamos que se hicieron las llamadas correctas
        verify(usuarioRepository, times(1))
                .existsByEmailIgnoreCase("ana@correo.com");
        verify(roleRepository, times(1))
                .findByNombreIgnoreCase("ROLE_EMPLEADO");
        verify(passwordEncoder, times(1))
                .encode("secret123");
        // Verificamos que se llamó a save con un Usuario cuyo email está en minúscula y password hasheado
        verify(usuarioRepository, times(1)).save(argThat(savedUser ->
                savedUser.getNombre().equals("Ana Gómez")
                        && savedUser.getEmail().equals("ana@correo.com")
                        && savedUser.getPassword().equals("hashedSecret")
                        && savedUser.getRole().equals(roleEmpleado)
        ));
    }

    @Test
    void testCrearUsuarioEmpleado_EmailAlreadyExists() {
        UsuarioRequestDto request = new UsuarioRequestDto();
        request.setNombre("Pedro Ruiz");
        request.setEmail("pedro@correo.com");
        request.setPassword("password");

        when(usuarioRepository.existsByEmailIgnoreCase("pedro@correo.com"))
                .thenReturn(true);

        // Debe lanzar EmailAlreadyExistsException y no continuar
        assertThrows(EmailAlreadyExistsException.class, () ->
                usuarioService.crearUsuarioEmpleado(request)
        );

        verify(usuarioRepository, times(1))
                .existsByEmailIgnoreCase("pedro@correo.com");
        verify(roleRepository, never()).findByNombreIgnoreCase(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void testCrearUsuarioEmpleado_RoleNotFound() {
        UsuarioRequestDto request = new UsuarioRequestDto();
        request.setNombre("Laura Díaz");
        request.setEmail("laura@correo.com");
        request.setPassword("pwd123");

        when(usuarioRepository.existsByEmailIgnoreCase("laura@correo.com"))
                .thenReturn(false);
        when(roleRepository.findByNombreIgnoreCase("ROLE_EMPLEADO"))
                .thenReturn(Optional.empty());

        // Debe lanzar RoleNotFoundException y no guardar
        assertThrows(RoleNotFoundException.class, () ->
                usuarioService.crearUsuarioEmpleado(request)
        );

        verify(usuarioRepository, times(1))
                .existsByEmailIgnoreCase("laura@correo.com");
        verify(roleRepository, times(1))
                .findByNombreIgnoreCase("ROLE_EMPLEADO");
        verify(usuarioRepository, never()).save(any());
    }

    // ====== listarUsuarios ======

    @Test
    void testListarUsuarios_Success() {
        // Creamos dos usuarios de ejemplo
        Usuario u1 = new Usuario();
        u1.setId(3L);
        u1.setNombre("María López");
        u1.setEmail("maria@correo.com");
        u1.setPassword("pwd");
        u1.setRole(roleEmpleado);

        Usuario u2 = new Usuario();
        u2.setId(4L);
        u2.setNombre("Carlos Ruiz");
        u2.setEmail("carlos@correo.com");
        u2.setPassword("pwd");
        u2.setRole(roleEmpleado);

        when(usuarioRepository.findAll())
                .thenReturn(List.of(u1, u2));

        List<UsuarioResponseDto> lista = usuarioService.listarUsuarios();

        assertEquals(2, lista.size());
        // Validar que el mapeo a DTO es correcto
        assertTrue(lista.stream().anyMatch(dto ->
                dto.getId().equals(3L)
                        && dto.getNombre().equals("María López")
                        && dto.getEmail().equals("maria@correo.com")
                        && dto.getRoleName().equals("ROLE_EMPLEADO")
        ));
        assertTrue(lista.stream().anyMatch(dto ->
                dto.getId().equals(4L)
                        && dto.getNombre().equals("Carlos Ruiz")
                        && dto.getEmail().equals("carlos@correo.com")
                        && dto.getRoleName().equals("ROLE_EMPLEADO")
        ));

        verify(usuarioRepository, times(1)).findAll();
    }

    @Test
    void testListarUsuarios_NoData() {
        when(usuarioRepository.findAll())
                .thenReturn(Collections.emptyList());

        assertThrows(NoDataFoundException.class, () ->
                usuarioService.listarUsuarios()
        );

        verify(usuarioRepository, times(1)).findAll();
    }
}