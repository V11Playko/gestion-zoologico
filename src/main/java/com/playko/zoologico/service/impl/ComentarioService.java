package com.playko.zoologico.service.impl;

import com.playko.zoologico.configuration.security.userDetails.CustomUserDetails;
import com.playko.zoologico.dto.request.ComentarioRequestDto;
import com.playko.zoologico.dto.response.ComentarioResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.comentario.ComentarioPadreNotFoundException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IComentarioRepository;
import com.playko.zoologico.repository.IUsuarioRepository;
import com.playko.zoologico.service.IComentarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ComentarioService implements IComentarioService {
    private final IComentarioRepository comentarioRepository;

    private final IAnimalRepository animalRepository;

    private final IUsuarioRepository usuarioRepository;

    @Override
    public void agregarComentario(ComentarioRequestDto dto) {
        Animal animal = animalRepository.findByNombreIgnoreCase(dto.getAnimalNombre())
                .orElseThrow(AnimalNotFoundException::new);

        String correoUsuarioAutenticado = obtenerCorreoDelToken();
        Usuario autor = usuarioRepository.findByEmail(correoUsuarioAutenticado);

        Comentario comentario = new Comentario();
        comentario.setContenido(dto.getContenido().trim());
        comentario.setFecha(LocalDateTime.now());
        comentario.setAnimal(animal);
        comentario.setAutor(autor);

        if (dto.getPadreId() != null) {
            Comentario padre = comentarioRepository.findById(dto.getPadreId())
                    .orElseThrow(ComentarioPadreNotFoundException::new);
            comentario.setPadre(padre);
        }

        comentarioRepository.save(comentario);
    }

    @Override
    public List<ComentarioResponseDto> obtenerMuroDeAnimal(String animalName) {
        Animal animal = animalRepository.findByNombreIgnoreCase(animalName)
                .orElseThrow(() -> new RuntimeException("Animal no encontrado"));

        List<Comentario> comentarios = comentarioRepository.findByAnimalAndPadreIsNullOrderByFechaAsc(animal);

        return comentarios.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    private ComentarioResponseDto mapToResponseDto(Comentario comentario) {
        List<ComentarioResponseDto> respuestasDto = comentario.getRespuestas() != null
                ? comentario.getRespuestas().stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList())
                : new ArrayList<>();

        return new ComentarioResponseDto(
                comentario.getId(),
                comentario.getContenido(),
                comentario.getFecha().toString(),
                comentario.getAutor().getNombre(),
                respuestasDto
        );
    }

    public String obtenerCorreoDelToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new RuntimeException("Error obteniendo el correo del token.");
    }
}
