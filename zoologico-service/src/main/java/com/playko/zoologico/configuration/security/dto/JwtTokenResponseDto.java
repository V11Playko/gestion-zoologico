package com.playko.zoologico.configuration.security.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JwtTokenResponseDto {
    private String jwtToken;
    private String email;
    private List<String> roles;
}

