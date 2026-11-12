package com.playko.zoologico.security;

import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
import com.playko.zoologico.entity.Role;
import com.playko.zoologico.entity.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CustomUserDetailsTest {

    private Usuario usuario;
    private Role roleAdmin;

    @BeforeEach
    void setUp() {
        roleAdmin = new Role();
        roleAdmin.setId(1L);
        roleAdmin.setNombre("ROLE_ADMIN");

        usuario = new Usuario();
        usuario.setId(99L);
        usuario.setEmail("admin@zoo.com");
        usuario.setPassword("hashed-password");
        usuario.setRole(roleAdmin);
    }

    /* ===== build ===== */

    @Test
    void build_shouldCreateCustomUserDetailsWithCorrectAuthorities() {
        CustomUserDetails details = CustomUserDetails.build(usuario, List.of(roleAdmin));

        assertThat(details.getUsername()).isEqualTo("admin@zoo.com");
        assertThat(details.getPassword()).isEqualTo("hashed-password");
        assertThat(details.getUsuario()).isEqualTo(usuario);

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertThat(authorities).hasSize(1);
        assertThat(authorities.iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    void build_shouldMapMultipleRolesToAuthorities() {
        Role roleEmpleado = new Role();
        roleEmpleado.setId(2L);
        roleEmpleado.setNombre("ROLE_EMPLEADO");

        CustomUserDetails details = CustomUserDetails.build(usuario, List.of(roleAdmin, roleEmpleado));

        assertThat(details.getAuthorities()).hasSize(2);
    }

    /* ===== UserDetails contract methods ===== */

    @Test
    void accountStatusMethods_shouldAlwaysReturnTrue() {
        CustomUserDetails details = CustomUserDetails.build(usuario, List.of(roleAdmin));

        assertThat(details.isAccountNonExpired()).isTrue();
        assertThat(details.isAccountNonLocked()).isTrue();
        assertThat(details.isCredentialsNonExpired()).isTrue();
        assertThat(details.isEnabled()).isTrue();
    }

    /* ===== equals y hashCode ===== */

    @Test
    void equals_shouldReturnTrue_forSameEmail() {
        CustomUserDetails a = CustomUserDetails.build(usuario, List.of(roleAdmin));
        CustomUserDetails b = CustomUserDetails.build(usuario, List.of(roleAdmin));

        assertThat(a).isEqualTo(b);
    }

    @Test
    void equals_shouldReturnFalse_forDifferentEmail() {
        Usuario other = new Usuario();
        other.setId(2L);
        other.setEmail("otro@zoo.com");
        other.setPassword("pass");

        CustomUserDetails a = CustomUserDetails.build(usuario, List.of(roleAdmin));
        CustomUserDetails b = CustomUserDetails.build(other, List.of(roleAdmin));

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void equals_shouldReturnFalse_forNull() {
        CustomUserDetails a = CustomUserDetails.build(usuario, List.of(roleAdmin));
        assertThat(a).isNotEqualTo(null);
    }

    @Test
    void equals_shouldReturnTrue_forSameInstance() {
        CustomUserDetails a = CustomUserDetails.build(usuario, List.of(roleAdmin));
        assertThat(a).isEqualTo(a);
    }

    @Test
    void hashCode_shouldBeConsistentWithEquals() {
        CustomUserDetails a = CustomUserDetails.build(usuario, List.of(roleAdmin));
        CustomUserDetails b = CustomUserDetails.build(usuario, List.of(roleAdmin));

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void equals_shouldReturnFalse_forDifferentClass() {
        CustomUserDetails a = CustomUserDetails.build(usuario, List.of(roleAdmin));
        // getClass() != o.getClass() — branch no cubierto previamente
        assertThat(a).isNotEqualTo("un-string");
    }
}
