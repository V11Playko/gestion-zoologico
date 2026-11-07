package com.playko.zoologico.security;

import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetailsService;
import com.playko.zoologico.entity.Role;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.repository.IUsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private IUsuarioRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Test
    void loadUserByUsername_shouldReturnCustomUserDetails_whenUserExists() {
        Role role = new Role();
        role.setId(1L);
        role.setNombre("ROLE_EMPLEADO");

        Usuario usuario = new Usuario();
        usuario.setId(10L);
        usuario.setEmail("empleado@zoo.com");
        usuario.setPassword("hashed");
        usuario.setRole(role);

        when(userRepository.findByEmailWithRole("empleado@zoo.com")).thenReturn(usuario);

        UserDetails result = userDetailsService.loadUserByUsername("empleado@zoo.com");

        assertThat(result).isInstanceOf(CustomUserDetails.class);
        assertThat(result.getUsername()).isEqualTo("empleado@zoo.com");
        assertThat(result.getAuthorities()).hasSize(1);
        assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_EMPLEADO");
    }

    @Test
    void loadUserByUsername_shouldThrowUsernameNotFoundException_whenUserNotFound() {
        when(userRepository.findByEmailWithRole("noexiste@zoo.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("noexiste@zoo.com"));
    }
}
