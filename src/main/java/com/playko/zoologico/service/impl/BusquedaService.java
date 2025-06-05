package com.playko.zoologico.service.impl;

import com.playko.zoologico.dto.response.BusquedaResultadoDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Especie;
import com.playko.zoologico.entity.Zona;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IComentarioRepository;
import com.playko.zoologico.repository.IEspecieRepository;
import com.playko.zoologico.repository.IZonaRepository;
import com.playko.zoologico.service.IBusquedaService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class BusquedaService implements IBusquedaService {
    private final IZonaRepository zonaRepository;
    private final IEspecieRepository especieRepository;
    private final IAnimalRepository animalRepository;
    private final IComentarioRepository comentarioRepository;

    @Override
    public List<BusquedaResultadoDto> buscarPorPalabra(String palabra) {
        String like = palabra.toLowerCase();

        List<BusquedaResultadoDto> resultados = new ArrayList<>();

        // Buscar zonas
        List<Zona> zonas = zonaRepository.findAll().stream()
                .filter(z -> z.getNombre().toLowerCase().contains(like))
                .toList();

        for (Zona zona : zonas) {
            resultados.add(new BusquedaResultadoDto("ZONA", zona.getNombre(), null, null, null, null));
        }

        // Buscar especies
        List<Especie> especies = especieRepository.findAll().stream()
                .filter(e -> e.getNombre().toLowerCase().contains(like))
                .toList();

        for (Especie especie : especies) {
            resultados.add(new BusquedaResultadoDto("ESPECIE",
                    especie.getZona().getNombre(),
                    especie.getNombre(),
                    null,
                    null,
                    null));
        }

        // Buscar animales
        List<Animal> animales = animalRepository.findAll().stream()
                .filter(a -> a.getNombre().toLowerCase().contains(like))
                .toList();

        for (Animal animal : animales) {
            resultados.add(new BusquedaResultadoDto("ANIMAL",
                    animal.getEspecie().getZona().getNombre(),
                    animal.getEspecie().getNombre(),
                    animal.getNombre(),
                    null,
                    null));
        }

        // Buscar comentarios y respuestas (contenido)
        List<Comentario> comentarios = comentarioRepository.findAll();

        for (Comentario c : comentarios) {
            String contenidoLower = c.getContenido().toLowerCase();
            if (contenidoLower.contains(like)) {
                // Si comentario no tiene padre → es comentario padre
                if (c.getPadre() == null) {
                    // Comentario padre que coincide
                    resultados.add(new BusquedaResultadoDto("COMENTARIO",
                            c.getAnimal().getEspecie().getZona().getNombre(),
                            c.getAnimal().getEspecie().getNombre(),
                            c.getAnimal().getNombre(),
                            c.getContenido(),
                            null));
                } else {
                    // Es respuesta
                    Comentario padre = c.getPadre();
                    resultados.add(new BusquedaResultadoDto("RESPUESTA",
                            padre.getAnimal().getEspecie().getZona().getNombre(),
                            padre.getAnimal().getEspecie().getNombre(),
                            padre.getAnimal().getNombre(),
                            padre.getContenido(),
                            c.getContenido()));
                }
            }
        }

        return resultados;
    }
}
