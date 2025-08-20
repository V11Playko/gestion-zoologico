package com.playko.zoologico.service.impl;

import com.playko.zoologico.dto.request.UsuarioRequestDto;
import com.playko.zoologico.dto.response.UsuarioResponseDto;
import com.playko.zoologico.entity.Role;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.usuario.EmailAlreadyExistsException;
import com.playko.zoologico.exception.usuario.RoleNotFoundException;
import com.playko.zoologico.repository.IRoleRepository;
import com.playko.zoologico.repository.IUsuarioRepository;
import com.playko.zoologico.service.IUsuarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService {
    private final IUsuarioRepository usuarioRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void crearUsuario(UsuarioRequestDto dto, String nombreRol) {
        if (usuarioRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        Role role = roleRepository.findByNombreIgnoreCase(nombreRol)
                .orElseThrow(RoleNotFoundException::new);

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRole(role);

        usuarioRepository.save(usuario);
    }

    @Override
    public void crearUsuarioAdmin(UsuarioRequestDto dto) {
        crearUsuario(dto, "ROLE_ADMIN");
    }

    @Override
    public void crearUsuarioEmpleado(UsuarioRequestDto dto) {
        crearUsuario(dto, "ROLE_EMPLEADO");
    }

    @Override
    public void crearUsuarioCliente(UsuarioRequestDto dto) {
        crearUsuario(dto, "ROLE_CLIENTE");
    }


    @Override
    public List<UsuarioResponseDto> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        if (usuarios.isEmpty()) throw new NoDataFoundException();

        return usuarios.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    private UsuarioResponseDto mapToResponseDto(Usuario usuario) {
        return new UsuarioResponseDto(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRole().getNombre()
        );
    }
}
