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
import com.playko.zoologico.exception.FechaFormatoInvalidoException;
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
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
    public byte[] generarExcelComentariosPorFecha(String fechaStr) {
        LocalDate queryDate;
        ZoneId zone = ZoneId.of("America/Bogota");

        // Validación del formato de fecha
        try {
            if (fechaStr != null && !fechaStr.isEmpty()) {
                queryDate = LocalDate.parse(fechaStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } else {
                queryDate = LocalDate.now(zone);
            }
        } catch (DateTimeParseException e) {
            throw new FechaFormatoInvalidoException(fechaStr);
        }

        LocalDateTime startOfDay = queryDate.atStartOfDay();
        LocalDateTime endOfDay = queryDate.atTime(LocalTime.MAX);
        List<Comentario> comentarios = comentarioRepository.findByFechaBetween(startOfDay, endOfDay);

        if (comentarios == null || comentarios.isEmpty()) {
            throw new NoComentariosEnFechaException(queryDate);
        }

        DateTimeFormatter dtf = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Map<String, CellStyle> estilos = crearEstilos(workbook);
            hojaPorComentarios(workbook, queryDate, estilos, comentarios, dtf);
            hojaPorEstadisticas(workbook, comentarios, estilos, dtf);
            hojaPorUsuario(workbook, comentarios, estilos);
            hojaPorAnimal(workbook, comentarios, estilos);
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new ErrorGeneratingExcelException();
        }
    }


    private void hojaPorComentarios(Workbook workbook,
                                    LocalDate queryDate,
                                    Map<String, CellStyle> estilos,
                                    List<Comentario> comentarios,
                                    DateTimeFormatter dtf) {

        CellStyle headerStyle = estilos.get("header");
        CellStyle cellStyle = estilos.get("normal");

        Sheet sheet = workbook.createSheet("Comentarios-" + queryDate);
        String[] headers = {
                "ComentarioId", "Contenido", "Fecha",
                "AutorId", "AutorNombre", "AutorEmail",
                "AnimalId", "AnimalNombre",
                "PadreId", "EsRespuesta",
                "CreadorAnimalId", "CreadorAnimalNombre", "CreadorAnimalEmail"
        };

        crearHeader(sheet, headers, headerStyle);

        int rowIdx = 1;
        for (Comentario c : comentarios) {
            Row r = sheet.createRow(rowIdx++);
            Usuario autor = c.getAutor();
            Animal animal = c.getAnimal();
            Usuario creadorAnimal = animal != null ? animal.getCreador() : null;
            Comentario padre = c.getPadre();

            int col = 0;
            setCell(r, col++, c.getId(), cellStyle);
            setCell(r, col++, nullSafeTrim(c.getContenido()), cellStyle);
            setCell(r, col++, c.getFecha() != null ? c.getFecha().format(dtf) : "", cellStyle);

            setCell(r, col++, autor != null && autor.getId() != null ? autor.getId() : null, cellStyle);
            setCell(r, col++, autor != null ? autor.getNombre() : "", cellStyle);
            setCell(r, col++, autor != null ? autor.getEmail() : "", cellStyle);

            setCell(r, col++, animal != null && animal.getId() != null ? animal.getId() : null, cellStyle);
            setCell(r, col++, animal != null ? animal.getNombre() : "", cellStyle);

            setCell(r, col++, padre != null && padre.getId() != null ? padre.getId() : null, cellStyle);
            setCell(r, col++, padre != null, cellStyle);

            setCell(r, col++, creadorAnimal != null && creadorAnimal.getId() != null ? creadorAnimal.getId() : null, cellStyle);
            setCell(r, col++, creadorAnimal != null ? creadorAnimal.getNombre() : "", cellStyle);
            setCell(r, col++, creadorAnimal != null ? creadorAnimal.getEmail() : "", cellStyle);
        }

        for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
    }

    private void hojaPorEstadisticas(Workbook workbook,
                                     List<Comentario> comentarios,
                                     Map<String, CellStyle> estilos,
                                     DateTimeFormatter dtf) {

        CellStyle headerStyle = estilos.get("header");
        CellStyle cellStyle = estilos.get("normal");
        CellStyle percentStyle = estilos.get("percent");

        long total = comentarios.size();
        long respondidos = comentarios.stream().filter(c -> c.getPadre() != null).count();
        double porcentajeRespondidos = total > 0 ? (respondidos * 1.0 / total) : 0.0;

        Map<String, Long> comentariosPorEmail = comentarios.stream()
                .filter(c -> c.getAutor() != null && c.getAutor().getEmail() != null)
                .collect(Collectors.groupingBy(c -> c.getAutor().getEmail(), Collectors.counting()));
        Map.Entry<String, Long> topEmailEntry = comentariosPorEmail.entrySet().stream()
                .max(Map.Entry.comparingByValue()).orElse(null);
        String emailTop = topEmailEntry != null ? topEmailEntry.getKey() + " (" + topEmailEntry.getValue() + ")" : "N/A";

        Map<String, Long> comentariosPorAnimal = comentarios.stream()
                .filter(c -> c.getAnimal() != null && c.getAnimal().getNombre() != null)
                .collect(Collectors.groupingBy(c -> c.getAnimal().getNombre(), Collectors.counting()));
        Map.Entry<String, Long> topAnimalEntry = comentariosPorAnimal.entrySet().stream()
                .max(Map.Entry.comparingByValue()).orElse(null);
        String animalTop = topAnimalEntry != null ? topAnimalEntry.getKey() + " (" + topAnimalEntry.getValue() + " comentarios)" : "N/A";

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

        Sheet statsSheet = workbook.createSheet("Estadísticas");
        String[][] stats = {
                {"Total de comentarios", String.valueOf(total)},
                {"Total de respondidos", String.valueOf(respondidos)},
                {"% Respondidos", null}, // valor numérico se setea aparte
                {"Autor más activo (email - count)", emailTop},
                {"Animal con más comentarios", animalTop},
                {"Primer comentario del día", primerComentario},
                {"Último comentario del día", ultimoComentario},
                {"Promedio caracteres/comentario", String.format("%.2f", promedioCaracteres)}
        };

        for (int i = 0; i < stats.length; i++) {
            Row r = statsSheet.createRow(i);
            Cell keyCell = r.createCell(0);
            keyCell.setCellValue(stats[i][0]);
            keyCell.setCellStyle(headerStyle);

            Cell valueCell = r.createCell(1);
            if (i == 2) { // porcentaje
                valueCell.setCellValue(porcentajeRespondidos);
                valueCell.setCellStyle(percentStyle);
            } else {
                valueCell.setCellValue(stats[i][1] != null ? stats[i][1] : "");
                valueCell.setCellStyle(cellStyle);
            }
        }

        statsSheet.autoSizeColumn(0);
        statsSheet.autoSizeColumn(1);
    }

    private void hojaPorUsuario(Workbook workbook,
                                List<Comentario> comentarios,
                                Map<String, CellStyle> estilos) {
        CellStyle headerStyle = estilos.get("header");
        CellStyle cellStyle = estilos.get("normal");

        Sheet userSheet = workbook.createSheet("Por Usuario");
        String[] userHeaders = {"AutorNombre", "AutorEmail", "# Comentarios", "# Respuestas hechas", "# Respuestas recibidas"};
        crearHeader(userSheet, userHeaders, headerStyle);

        // Agrupar por autorId
        Map<Long, List<Comentario>> comentariosPorAutorId = comentarios.stream()
                .filter(c -> c.getAutor() != null && c.getAutor().getId() != null)
                .collect(Collectors.groupingBy(c -> c.getAutor().getId()));

        int row = 1;
        for (Map.Entry<Long, List<Comentario>> e : comentariosPorAutorId.entrySet()) {
            List<Comentario> lista = e.getValue();
            Usuario u = lista.get(0).getAutor();
            long totalByUser = lista.size();
            long respuestasHechas = lista.stream().filter(c -> c.getPadre() != null).count();

            long respuestasRecibidas = comentarios.stream()
                    .filter(c -> c.getPadre() != null && c.getPadre().getAutor() != null
                            && Objects.equals(c.getPadre().getAutor().getId(), u.getId()))
                    .count();

            Row r = userSheet.createRow(row++);
            int col = 0;
            setCell(r, col++, safeString(u.getNombre()), cellStyle);
            setCell(r, col++, safeString(u.getEmail()), cellStyle);
            setCell(r, col++, totalByUser, cellStyle);
            setCell(r, col++, respuestasHechas, cellStyle);
            setCell(r, col++, respuestasRecibidas, cellStyle);
        }

        for (int i = 0; i < userHeaders.length; i++) userSheet.autoSizeColumn(i);
    }

    private void hojaPorAnimal(Workbook workbook,
                               List<Comentario> comentarios,
                               Map<String, CellStyle> estilos) {
        CellStyle headerStyle = estilos.get("header");
        CellStyle cellStyle = estilos.get("normal");
        CellStyle percentStyle = estilos.get("percent");

        Sheet animalSheet = workbook.createSheet("Por Animal");
        String[] animalHeaders = {"AnimalNombre", "# Comentarios", "# Usuarios distintos", "% Respondidos (en animal)"};
        crearHeader(animalSheet, animalHeaders, headerStyle);

        Map<Long, List<Comentario>> comentariosPorAnimalId = comentarios.stream()
                .filter(c -> c.getAnimal() != null && c.getAnimal().getId() != null)
                .collect(Collectors.groupingBy(c -> c.getAnimal().getId()));

        int row = 1;
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

            Row r = animalSheet.createRow(row++);
            int col = 0;
            setCell(r, col++, safeString(a != null ? a.getNombre() : ""), cellStyle);
            setCell(r, col++, totalAnimal, cellStyle);
            setCell(r, col++, usuariosDistintos, cellStyle);
            // porcentaje como número con formato
            Cell pctCell = r.createCell(col++);
            pctCell.setCellValue(pctRespondidosAnimal);
            pctCell.setCellStyle(percentStyle);
        }

        for (int i = 0; i < animalHeaders.length; i++) animalSheet.autoSizeColumn(i);
    }

    private Map<String, CellStyle> crearEstilos(Workbook workbook) {
        Map<String, CellStyle> estilos = new HashMap<>();

        // header
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerStyle.setFont(headerFont);
        setBorders(headerStyle);
        estilos.put("header", headerStyle);

        // normal
        CellStyle normal = workbook.createCellStyle();
        setBorders(normal);
        normal.setWrapText(true);
        estilos.put("normal", normal);

        // percent
        CellStyle percent = workbook.createCellStyle();
        percent.cloneStyleFrom(normal);
        DataFormat df = workbook.createDataFormat();
        percent.setDataFormat(df.getFormat("0.00%"));
        estilos.put("percent", percent);

        return estilos;
    }

    private void setBorders(CellStyle style) {
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }

    private void crearHeader(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell c = headerRow.createCell(i);
            c.setCellValue(headers[i]);
            c.setCellStyle(headerStyle);
        }
    }

    private void setCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
        if (style != null) cell.setCellStyle(style);
    }

    private String nullSafeTrim(String s) {
        return s == null ? "" : s.trim();
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
