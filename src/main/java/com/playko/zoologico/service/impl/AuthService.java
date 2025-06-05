package com.playko.zoologico.service.impl;

import com.playko.zoologico.configuration.security.dto.JwtTokenResponseDto;
import com.playko.zoologico.configuration.security.dto.LoginRequestDto;
import com.playko.zoologico.configuration.security.jwt.JwtUtils;
import com.playko.zoologico.configuration.security.userDetails.CustomUserDetails;
import com.playko.zoologico.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    @Override
    public JwtTokenResponseDto loginUser(LoginRequestDto loginRequestDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return new JwtTokenResponseDto(jwt, userDetails.getUsername(), roles);
    }
}