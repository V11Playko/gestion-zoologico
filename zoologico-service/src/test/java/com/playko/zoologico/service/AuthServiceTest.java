package com.playko.zoologico.service;

import com.playko.zoologico.configuration.security.dto.JwtTokenResponseDto;
import com.playko.zoologico.configuration.security.dto.LoginRequestDto;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
import com.playko.zoologico.service.impl.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginUser_shouldReturnJwtTokenResponseDto_withRolesAndEmail() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("admin@zoo.com");
        request.setPassword("pass123");

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN")
        );
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUsername()).thenReturn("admin@zoo.com");
        when(userDetails.getAuthorities()).thenAnswer(inv -> authorities);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("jwt-token-generado");

        JwtTokenResponseDto result = authService.loginUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getToken()).isEqualTo("jwt-token-generado");
        assertThat(result.getEmail()).isEqualTo("admin@zoo.com");
        assertThat(result.getRoles()).containsExactly("ROLE_ADMIN");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateJwtToken(auth);
    }

    @Test
    void loginUser_shouldIncludeMultipleRoles_whenUserHasMany() {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("multi@zoo.com");
        request.setPassword("pwd");

        List<SimpleGrantedAuthority> authorities = List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_EMPLEADO")
        );
        CustomUserDetails userDetails = mock(CustomUserDetails.class);
        when(userDetails.getUsername()).thenReturn("multi@zoo.com");
        when(userDetails.getAuthorities()).thenAnswer(inv -> authorities);

        Authentication auth = mock(Authentication.class);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtUtils.generateJwtToken(auth)).thenReturn("token-multi");

        JwtTokenResponseDto result = authService.loginUser(request);

        assertThat(result.getRoles()).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_EMPLEADO");
    }
}
