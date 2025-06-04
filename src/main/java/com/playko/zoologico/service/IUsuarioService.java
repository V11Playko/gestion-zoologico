package com.playko.zoologico.service;

import com.playko.zoologico.dto.request.UsuarioRequestDto;
import com.playko.zoologico.dto.response.UsuarioResponseDto;
import com.playko.zoologico.entity.Usuario;

import java.util.List;

public interface IUsuarioService {
    void crearUsuarioEmpleado(UsuarioRequestDto dto);
    List<UsuarioResponseDto> listarUsuarios();
}
