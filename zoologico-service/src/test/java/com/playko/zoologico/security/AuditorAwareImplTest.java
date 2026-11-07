package com.playko.zoologico.security;

import com.playko.zoologico.configuration.security.AuditorAwareImpl;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
import com.playko.zoologico.entity.Role;
import com.playko.zoologico.entity.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditorAwareImplTest {

    private AuditorAwareImpl auditorAware;
    private Usuario usuario;

    @BeforeEach
    void setUp() {
        auditorAware = new AuditorAwareImpl();

        Role role = new Role();
        role.setId(1L);
        role.setNombre("ROLE_ADMIN");

        usuario = new Usuario();
        usuario.setId(5L);
        usuario.setEmail("admin@zoo.com");
        usuario.setRole(role);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_shouldReturnEmpty_whenAuthenticationIsNull() {
        SecurityContextHolder.clearContext();

        Optional<Usuario> result = auditorAware.getCurrentAuditor();

        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentAuditor_shouldReturnEmpty_whenNotAuthenticated() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Usuario> result = auditorAware.getCurrentAuditor();

        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentAuditor_shouldReturnEmpty_whenPrincipalIsAnonymousUser() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("anonymousUser");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Usuario> result = auditorAware.getCurrentAuditor();

        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentAuditor_shouldReturnUsuario_whenPrincipalIsCustomUserDetails() {
        CustomUserDetails userDetails = new CustomUserDetails(
                "admin@zoo.com", "hashed",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN")),
                usuario
        );

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Usuario> result = auditorAware.getCurrentAuditor();

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(usuario);
    }

    @Test
    void getCurrentAuditor_shouldReturnEmpty_whenPrincipalIsNotCustomUserDetails() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn("otro-tipo-de-principal");
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Usuario> result = auditorAware.getCurrentAuditor();

        assertThat(result).isEmpty();
    }
}
