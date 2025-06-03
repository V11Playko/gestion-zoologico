package com.playko.zoologico.configuration.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {
    @NotBlank(message = "El email no debe estar en blanco")
    @Email(message = "El campo 'email' debe ser una dirección de correo electrónico válida. Ingrese el formato nombre@ejemplo.com")
    private String email;

    @NotBlank(message = "La password no puede ser nula")
    @Size(min = 4, max = 255, message = "La password debe tener entre 4 y 255 caracteres")
    private String password;
}
