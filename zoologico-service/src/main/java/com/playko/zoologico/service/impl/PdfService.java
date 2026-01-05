package com.playko.zoologico.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IComentarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PdfService {

    private final IComentarioRepository comentarioRepository;
    private final IAnimalRepository animalRepository;
    private final TemplateEngine templateEngine;

    @Value("${reports.output-dir:reports}")
    private String outputDir;

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    @Scheduled(cron = "0 25 14 * * *", zone = "America/Bogota")
    public void generarReportesDiarios() {
        LocalDate hoy = LocalDate.now(ZONE);
        generarReportesParaFecha(hoy);
    }

    public void generarReportesParaFecha(LocalDate fecha) {
        Map<String, PdfInfo> reportes = generarReportesEnMemoria(fecha);

        // Guardar cada reporte en disco (si deseas mantenerlo además de subir a S3)
        reportes.forEach((fileName, info) -> {
            try {
                if (!java.nio.file.Files.exists(java.nio.file.Paths.get(outputDir))) {
                    java.nio.file.Files.createDirectories(java.nio.file.Paths.get(outputDir));
                }
                java.nio.file.Path outPath = java.nio.file.Paths.get(outputDir, fileName);
                java.nio.file.Files.write(outPath, info.getBytes());
                log.info("📄 PDF guardado en {}", outPath.toAbsolutePath());
            } catch (Exception ex) {
                log.error("❌ No se pudo escribir PDF {}: {}", fileName, ex.getMessage(), ex);
            }
        });
    }

    public Map<String, PdfInfo> generarReportesEnMemoria(LocalDate fecha) {
        LocalDateTime startOfDay = fecha.atStartOfDay();
        LocalDateTime endOfDay = fecha.atTime(LocalTime.MAX);

        List<Comentario> comentariosHoy = comentarioRepository.findByFechaBetween(startOfDay, endOfDay);
        List<Animal> animales = animalRepository.findAll();

        Map<Long, List<Comentario>> comentariosPorAnimal = comentariosHoy.stream()
                .filter(c -> c.getAnimal() != null && c.getAnimal().getId() != null)
                .collect(Collectors.groupingBy(c -> c.getAnimal().getId()));

        Map<Usuario, List<Animal>> animalesPorCreador = animales.stream()
                .filter(a -> a.getCreatedBy() != null)
                .collect(Collectors.groupingBy(Animal::getCreatedBy));

        DateTimeFormatter fechaFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        Map<String, PdfInfo> reportes = new HashMap<>();

        for (Map.Entry<Usuario, List<Animal>> entry : animalesPorCreador.entrySet()) {
            Usuario creador = entry.getKey();
            List<Animal> animalesDelCreador = entry.getValue();

            List<Map<String, Object>> animalesDto = animalesDelCreador.stream().map(animal -> {
                Map<String, Object> m = new HashMap<>();
                m.put("animalId", animal.getId());
                m.put("animalNombre", animal.getNombre() != null ? animal.getNombre() : "N/A");
                List<Comentario> comentariosDeEsteAnimal = comentariosPorAnimal.getOrDefault(animal.getId(), Collections.emptyList());
                if (comentariosDeEsteAnimal.isEmpty()) {
                    m.put("comentarios", Collections.emptyList());
                    m.put("mensajeSinComentarios", "No hay comentarios sobre este animal hoy.");
                } else {
                    m.put("comentarios", comentariosDeEsteAnimal);
                    m.put("mensajeSinComentarios", null);
                }
                return m;
            }).toList();

            Context context = new Context();
            context.setVariable("creadorNombre", creador.getNombre() != null ? creador.getNombre() : creador.getEmail());
            context.setVariable("creadorEmail", creador.getEmail());
            context.setVariable("fecha", fecha.format(fechaFmt));
            context.setVariable("animales", animalesDto);

            String html = templateEngine.process("reporte-por-creador", context);

            try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, "");
                builder.toStream(os);
                builder.run();

                String safeName = (creador.getNombre() != null ? creador.getNombre() : creador.getEmail())
                        .replaceAll("[^a-zA-Z0-9\\-_\\.]", "_");
                String fileName = String.format("reporte-%s-%s.pdf", safeName, fecha.format(fechaFmt));

                PdfInfo info = new PdfInfo(os.toByteArray(), creador.getEmail());
                reportes.put(fileName, info);
            } catch (Exception e) {
                log.error("⚠️ Error generando PDF para {}: {}", creador.getEmail(), e.getMessage(), e);
            }
        }

        return reportes;
    }

    public static class PdfInfo {
        private final byte[] bytes;
        private final String creatorEmail;

        public PdfInfo(byte[] bytes, String creatorEmail) {
            this.bytes = bytes;
            this.creatorEmail = creatorEmail;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public String getCreatorEmail() {
            return creatorEmail;
        }
    }
}