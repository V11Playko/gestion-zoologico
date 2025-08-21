package com.playko.zoologico.service.impl;

import com.playko.zoologico.client.MessagingClient;
import com.playko.zoologico.client.dto.SendNotification;
import com.playko.zoologico.configuration.security.userdetails.CustomUserDetails;
import com.playko.zoologico.dto.request.ComentarioRequestDto;
import com.playko.zoologico.dto.response.ComentarioResponseDto;
import com.playko.zoologico.dto.response.PorcentajeComentariosConRespuestasDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.exception.ErrorGettingMailTokenException;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.animal.AnimalSinComentariosException;
import com.playko.zoologico.exception.comentario.ComentarioAnimalMismatchException;
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

@Service
@Transactional
@RequiredArgsConstructor
public class ComentarioService implements IComentarioService {
    private final IComentarioRepository comentarioRepository;

    private final IAnimalRepository animalRepository;
    private final IUsuarioRepository usuarioRepository;
    private final MessagingClient messagingClient;

    @Override
    public void agregarComentario(ComentarioRequestDto dto) {
        if (dto.getPadreId() != null) {
            comentarioRepository.findById(dto.getPadreId())
                    .orElseThrow(ComentarioPadreNotFoundException::new);
        }

        Animal animal = animalRepository.findById(dto.getAnimalId())
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

            if (!padre.getAnimal().getId().equals(animal.getId())) {
                throw new ComentarioAnimalMismatchException();
            }
            comentario.setPadre(padre);
        }

        Usuario creadorAnimal = animal.getCreador();
        if (!autor.getId().equals(creadorAnimal.getId())) {
            SendNotification notification = new SendNotification(
                    creadorAnimal.getEmail(),
                    "Nuevo comentario sobre el animal '" + animal.getNombre() + "'",
                    comentario.getContenido(),
                    animal.getId(),
                    animal.getNombre(),
                    comentario.getId(),
                    comentario.getFecha(),
                    autor.getNombre(),
                    autor.getEmail()
            );
            messagingClient.sendNotification(notification);
        }

        comentarioRepository.save(comentario);
    }

    @Override
    public List<ComentarioResponseDto> obtenerMuroDeAnimal(Long animalId) {
        Animal animal = animalRepository.findById(animalId)
                .orElseThrow(AnimalNotFoundException::new);

        List<Comentario> comentarios = comentarioRepository.findByAnimalAndPadreIsNullOrderByFechaAsc(animal);

        boolean tieneComentarios = comentarioRepository.existsByAnimal_Id(animal.getId());
        if (!tieneComentarios) {
            throw new AnimalSinComentariosException();
        }

        return comentarios.stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public PorcentajeComentariosConRespuestasDto obtenerPorcentajeComentariosConRespuestas() {
        List<Comentario> comentariosPadre = comentarioRepository.findByPadreIsNull();

        if (comentariosPadre.isEmpty()) {
            return new PorcentajeComentariosConRespuestasDto("0.0%");
        }

        long conRespuestas = comentariosPadre.stream()
                .filter(comentario -> comentario.getRespuestas() != null && !comentario.getRespuestas().isEmpty())
                .count();

        double porcentaje = (double) conRespuestas / comentariosPadre.size() * 100;

        String porcentajeFormateado = String.format("%.1f%%", porcentaje);

        return new PorcentajeComentariosConRespuestasDto(porcentajeFormateado);
    }

    private ComentarioResponseDto mapToResponseDto(Comentario comentario) {
        List<ComentarioResponseDto> respuestasDto = comentario.getRespuestas() != null
                ? comentario.getRespuestas().stream()
                .map(this::mapToResponseDto)
                .toList()
                : new ArrayList<>();

        return new ComentarioResponseDto(
                comentario.getId(),
                comentario.getContenido(),
                comentario.getFecha().toString(),
                comentario.getAutor().getId(),
                respuestasDto
        );
    }

    public String obtenerCorreoDelToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUsername();
        }
        throw new ErrorGettingMailTokenException();
    }
}
