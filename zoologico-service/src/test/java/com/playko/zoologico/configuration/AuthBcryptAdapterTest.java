package com.playko.zoologico.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthBcryptAdapterTest {

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthBcryptAdapter adapter;

    @Test
    void encodePassword_shouldDelegateToPasswordEncoder() {
        when(encoder.encode("plain")).thenReturn("$2a$hash");

        String result = adapter.encodePassword("plain");

        assertThat(result).isEqualTo("$2a$hash");
        verify(encoder).encode("plain");
    }

    @Test
    void decodePassword_shouldReturnNull() {
        String result = adapter.decodePassword("cualquier-hash");

        assertThat(result).isNull();
    }
}
