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
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UsuarioService implements IUsuarioService {
    private final IUsuarioRepository usuarioRepository;
    private final IRoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void crearUsuarioEmpleado(UsuarioRequestDto dto) {
        if (usuarioRepository.existsByEmailIgnoreCase(dto.getEmail())) {
            throw new EmailAlreadyExistsException();
        }

        Role roleEmpleado = roleRepository.findByNombreIgnoreCase("ROLE_EMPLEADO")
                .orElseThrow(RoleNotFoundException::new);

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre().trim());
        usuario.setEmail(dto.getEmail().trim().toLowerCase());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setRole(roleEmpleado);

        usuarioRepository.save(usuario);
    }

    @Override
    public List<UsuarioResponseDto> listarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        if (usuarios.isEmpty()) throw new NoDataFoundException();

        return usuarios.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
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
