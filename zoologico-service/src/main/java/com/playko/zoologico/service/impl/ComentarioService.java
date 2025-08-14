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
import com.playko.zoologico.exception.ErrorGeneratingExcelException;
import com.playko.zoologico.exception.ErrorGettingMailTokenException;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.animal.AnimalSinComentariosException;
import com.playko.zoologico.exception.comentario.ComentarioAnimalMismatchException;
import com.playko.zoologico.exception.comentario.ComentarioPadreNotFoundException;
import com.playko.zoologico.exception.comentario.NoComentariosEnFechaException;
import com.playko.zoologico.repository.IAnimalRepository;
import com.playko.zoologico.repository.IComentarioRepository;
import com.playko.zoologico.repository.IUsuarioRepository;
import com.playko.zoologico.service.IComentarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

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


    @Override
    public byte[] generarExcelComentariosPorFecha(LocalDate fecha) {
        ZoneId zone = ZoneId.of("America/Bogota");
        LocalDate queryDate = (fecha != null) ? fecha : LocalDate.now(zone);

        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.atTime(LocalTime.MAX);

        List<Comentario> comentarios = comentarioRepository.findByFechaBetween(startOfDay, endOfDay);

        if (comentarios == null || comentarios.isEmpty()) {
            throw new NoComentariosEnFechaException(queryDate);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {

        /* =========================
           Estilos reutilizables
           ========================= */
            // Header style (bold)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Normal cell style (with borders)
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setWrapText(true);

            // Percent format
            CellStyle percentStyle = workbook.createCellStyle();
            percentStyle.cloneStyleFrom(cellStyle);
            DataFormat df = workbook.createDataFormat();
            percentStyle.setDataFormat(df.getFormat("0.00%"));

            /* === HOJA 1: DETALLE DE COMENTARIOS === */
            Sheet sheet = workbook.createSheet("Comentarios-" + queryDate);
            String[] headers = {
                    "ComentarioId", "Contenido", "Fecha",
                    "AutorId", "AutorNombre", "AutorEmail",
                    "AnimalId", "AnimalNombre",
                    "PadreId", "EsRespuesta",
                    "CreadorAnimalId", "CreadorAnimalNombre", "CreadorAnimalEmail"
            };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (Comentario c : comentarios) {
                Row r = sheet.createRow(rowIdx++);
                Usuario autor = c.getAutor();
                Animal animal = c.getAnimal();
                Usuario creadorAnimal = animal != null ? animal.getCreador() : null;
                Comentario padre = c.getPadre();

                int col = 0;
                Cell cell;

                cell = r.createCell(col++); cell.setCellValue(safeLongToString(c.getId())); cell.setCellStyle(cellStyle);
                cell = r.createCell(col++); cell.setCellValue(nullSafeTrim(c.getContenido())); cell.setCellStyle(cellStyle);
                cell = r.createCell(col++); cell.setCellValue(c.getFecha() != null ? c.getFecha().format(dtf) : ""); cell.setCellStyle(cellStyle);

                cell = r.createCell(col++); cell.setCellValue(autor != null && autor.getId() != null ? autor.getId().toString() : ""); cell.setCellStyle(cellStyle);
                cell = r.createCell(col++); cell.setCellValue(autor != null && autor.getNombre() != null ? autor.getNombre() : ""); cell.setCellStyle(cellStyle);
                cell = r.createCell(col++); cell.setCellValue(autor != null && autor.getEmail() != null ? autor.getEmail() : ""); cell.setCellStyle(cellStyle);

                cell = r.createCell(col++); cell.setCellValue(animal != null && animal.getId() != null ? animal.getId().toString() : ""); cell.setCellStyle(cellStyle);
                cell = r.createCell(col++); cell.setCellValue(animal != null && animal.getNombre() != null ? animal.getNombre() : ""); cell.setCellStyle(cellStyle);

                cell = r.createCell(col++); cell.setCellValue(padre != null && padre.getId() != null ? padre.getId().toString() : ""); cell.setCellStyle(cellStyle);
                cell = r.createCell(col++); cell.setCellValue(padre != null); cell.setCellStyle(cellStyle);

                cell = r.createCell(col++); cell.setCellValue(creadorAnimal != null && creadorAnimal.getId() != null ? creadorAnimal.getId().toString() : ""); cell.setCellStyle(cellStyle);
                cell = r.createCell(col++); cell.setCellValue(creadorAnimal != null && creadorAnimal.getNombre() != null ? creadorAnimal.getNombre() : ""); cell.setCellStyle(cellStyle);
                cell = r.createCell(col++); cell.setCellValue(creadorAnimal != null && creadorAnimal.getEmail() != null ? creadorAnimal.getEmail() : ""); cell.setCellStyle(cellStyle);
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);


        /* =========================
           CÁLCULOS PARA ESTADÍSTICAS
           ========================= */
            long total = comentarios.size();
            long respondidos = comentarios.stream().filter(c -> c.getPadre() != null).count();
            double porcentajeRespondidos = total > 0 ? (respondidos * 1.0 / total) : 0.0;

            // Comentarios por email (autor)
            Map<String, Long> comentariosPorEmail = comentarios.stream()
                    .filter(c -> c.getAutor() != null && c.getAutor().getEmail() != null)
                    .collect(Collectors.groupingBy(c -> c.getAutor().getEmail(), Collectors.counting()));
            Map.Entry<String, Long> topEmailEntry = comentariosPorEmail.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            String emailTop = topEmailEntry != null ? topEmailEntry.getKey() + " (" + topEmailEntry.getValue() + ")" : "N/A";

            // Comentarios por animal (por nombre)
            Map<String, Long> comentariosPorAnimal = comentarios.stream()
                    .filter(c -> c.getAnimal() != null && c.getAnimal().getNombre() != null)
                    .collect(Collectors.groupingBy(c -> c.getAnimal().getNombre(), Collectors.counting()));
            Map.Entry<String, Long> topAnimalEntry = comentariosPorAnimal.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            String animalTop = topAnimalEntry != null ? topAnimalEntry.getKey() + " (" + topAnimalEntry.getValue() + " comentarios)" : "N/A";

            // Primer / último comentario y autor, en formato ISO con nombre seguro
            String primerComentario = comentarios.stream()
                    .filter(c -> c.getFecha() != null)
                    .min(Comparator.comparing(Comentario::getFecha))
                    .map(c -> c.getFecha().format(dtf) + " (" + safeString(c.getAutor() != null ? c.getAutor().getNombre() : null) + ")")
                    .orElse("N/A");

            String ultimoComentario = comentarios.stream()
                    .filter(c -> c.getFecha() != null)
                    .max(Comparator.comparing(Comentario::getFecha))
                    .map(c -> c.getFecha().format(dtf) + " (" + safeString(c.getAutor() != null ? c.getAutor().getNombre() : null) + ")")
                    .orElse("N/A");

            double promedioCaracteres = comentarios.stream()
                    .map(Comentario::getContenido)
                    .filter(Objects::nonNull)
                    .mapToInt(String::length)
                    .average()
                    .orElse(0.0);


            /* === HOJA 2: ESTADÍSTICAS === */
            Sheet statsSheet = workbook.createSheet("Estadísticas");
            String[][] stats = {
                    {"Total de comentarios", String.valueOf(total)},
                    {"Total de respondidos", String.valueOf(respondidos)},
                    {"% Respondidos", String.format("%.2f", porcentajeRespondidos)}, // pondremos como número y luego formateamos
                    {"Autor más activo (email - count)", emailTop},
                    {"Animal con más comentarios", animalTop},
                    {"Primer comentario del día", primerComentario},
                    {"Último comentario del día", ultimoComentario},
                    {"Promedio caracteres/comentario", String.format("%.2f", promedioCaracteres)}
            };

            // Escribimos filas y aplicamos formatos
            for (int i = 0; i < stats.length; i++) {
                Row r = statsSheet.createRow(i);
                Cell k = r.createCell(0);
                k.setCellValue(stats[i][0]);
                k.setCellStyle(headerStyle);

                Cell v = r.createCell(1);
                // Si es la fila de porcentaje, escribimos valor numérico para que se pueda formatear
                if (i == 2) {
                    double pctValue = porcentajeRespondidos; // 0..1
                    v.setCellValue(pctValue);
                    v.setCellStyle(percentStyle);
                } else {
                    v.setCellValue(stats[i][1]);
                    v.setCellStyle(cellStyle);
                }
            }
            statsSheet.autoSizeColumn(0);
            statsSheet.autoSizeColumn(1);


            /* === HOJA 3: RESUMEN POR USUARIO === */
            Sheet userSheet = workbook.createSheet("Por Usuario");
            String[] userHeaders = {"AutorNombre", "AutorEmail", "# Comentarios", "# Respuestas hechas", "# Respuestas recibidas"};
            Row uh = userSheet.createRow(0);
            for (int i = 0; i < userHeaders.length; i++) {
                Cell cell = uh.createCell(i);
                cell.setCellValue(userHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            // Agrupar por autorId para evitar problemas con equals en entidades
            Map<Long, List<Comentario>> comentariosPorAutorId = comentarios.stream()
                    .filter(c -> c.getAutor() != null && c.getAutor().getId() != null)
                    .collect(Collectors.groupingBy(c -> c.getAutor().getId()));

            int ui = 1;
            for (Map.Entry<Long, List<Comentario>> e : comentariosPorAutorId.entrySet()) {
                List<Comentario> lista = e.getValue();
                Usuario u = lista.get(0).getAutor(); // hay al menos 1
                long totalByUser = lista.size();
                long respuestasHechas = lista.stream().filter(c -> c.getPadre() != null).count();

                // Respuestas recibidas: contar comentarios cuya padre pertenece a este usuario
                long respuestasRecibidas = comentarios.stream()
                        .filter(c -> c.getPadre() != null && c.getPadre().getAutor() != null
                                && Objects.equals(c.getPadre().getAutor().getId(), u.getId()))
                        .count();

                Row row = userSheet.createRow(ui++);
                Cell c0 = row.createCell(0); c0.setCellValue(safeString(u.getNombre())); c0.setCellStyle(cellStyle);
                Cell c1 = row.createCell(1); c1.setCellValue(safeString(u.getEmail())); c1.setCellStyle(cellStyle);
                Cell c2 = row.createCell(2); c2.setCellValue(totalByUser); c2.setCellStyle(cellStyle);
                Cell c3 = row.createCell(3); c3.setCellValue(respuestasHechas); c3.setCellStyle(cellStyle);
                Cell c4 = row.createCell(4); c4.setCellValue(respuestasRecibidas); c4.setCellStyle(cellStyle);
            }
            for (int i = 0; i < userHeaders.length; i++) userSheet.autoSizeColumn(i);


            /* === HOJA 4: RESUMEN POR ANIMAL === */
            Sheet animalSheet = workbook.createSheet("Por Animal");
            String[] animalHeaders = {"AnimalNombre", "# Comentarios", "# Usuarios distintos", "% Respondidos (en animal)"};
            Row ah = animalSheet.createRow(0);
            for (int i = 0; i < animalHeaders.length; i++) {
                Cell cell = ah.createCell(i);
                cell.setCellValue(animalHeaders[i]);
                cell.setCellStyle(headerStyle);
            }

            Map<Long, List<Comentario>> comentariosPorAnimalId = comentarios.stream()
                    .filter(c -> c.getAnimal() != null && c.getAnimal().getId() != null)
                    .collect(Collectors.groupingBy(c -> c.getAnimal().getId()));

            int ai = 1;
            for (Map.Entry<Long, List<Comentario>> e : comentariosPorAnimalId.entrySet()) {
                List<Comentario> lista = e.getValue();
                Animal a = lista.get(0).getAnimal();
                long totalAnimal = lista.size();
                long usuariosDistintos = lista.stream()
                        .map(Comentario::getAutor)
                        .filter(Objects::nonNull)
                        .map(Usuario::getId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .count();
                long respondidosAnimal = lista.stream().filter(c -> c.getPadre() != null).count();
                double pctRespondidosAnimal = totalAnimal > 0 ? (respondidosAnimal * 1.0 / totalAnimal) : 0.0;

                Row row = animalSheet.createRow(ai++);
                Cell ca0 = row.createCell(0); ca0.setCellValue(a != null ? safeString(a.getNombre()) : ""); ca0.setCellStyle(cellStyle);
                Cell ca1 = row.createCell(1); ca1.setCellValue(totalAnimal); ca1.setCellStyle(cellStyle);
                Cell ca2 = row.createCell(2); ca2.setCellValue(usuariosDistintos); ca2.setCellStyle(cellStyle);
                Cell ca3 = row.createCell(3); ca3.setCellValue(pctRespondidosAnimal); ca3.setCellStyle(percentStyle);
            }
            for (int i = 0; i < animalHeaders.length; i++) animalSheet.autoSizeColumn(i);


            /* === FIN: escribir workbook y devolver bytes === */
            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            // Puedes loguear e.getMessage() antes de lanzar
            throw new ErrorGeneratingExcelException();
        }
    }

    /* --------------------
       Helpers
       -------------------- */
    private String nullSafeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private String safeLongToString(Long l) {
        return l == null ? "" : l.toString();
    }

    private String safeString(String s) {
        return s == null ? "" : s;
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
