package com.playko.zoologico.service;

import com.playko.zoologico.configuration.security.dto.JwtTokenResponseDto;
import com.playko.zoologico.configuration.security.dto.LoginRequestDto;

public interface IAuthHandler {
    JwtTokenResponseDto loginUser(LoginRequestDto loginRequestDto);
}
