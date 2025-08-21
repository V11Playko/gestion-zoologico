package com.playko.zoologico.service.impl;

import com.playko.zoologico.dto.response.ComentarioResponseDto;
import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.exception.ErrorGeneratingExcelException;
import com.playko.zoologico.exception.FechaFormatoInvalidoException;
import com.playko.zoologico.exception.comentario.NoComentariosEnFechaException;
import com.playko.zoologico.repository.IComentarioRepository;
import com.playko.zoologico.service.IExcelService;
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

import static com.playko.zoologico.constants.ExcelConstants.HEADER_STYLE;
import static com.playko.zoologico.constants.ExcelConstants.NORMAL_STYLE;
import static com.playko.zoologico.constants.ExcelConstants.PERCENT_STYLE;

@Service
@Transactional
@RequiredArgsConstructor
public class ExcelService implements IExcelService {

    private final IComentarioRepository comentarioRepository;

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

        CellStyle headerStyle = estilos.get(HEADER_STYLE);
        CellStyle cellStyle = estilos.get(NORMAL_STYLE);
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
            Row row = sheet.createRow(rowIdx++);
            setComentarioRow(row, c, cellStyle, dtf);
        }

        // Ajustar tamaño de columnas
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void setComentarioRow(Row row, Comentario c, CellStyle cellStyle, DateTimeFormatter dtf) {
        Usuario autor = c.getAutor();
        Animal animal = c.getAnimal();
        Usuario creadorAnimal = safeGetCreador(animal);
        Comentario padre = c.getPadre();

        setCell(row, 0, c.getId(), cellStyle);
        setCell(row, 1, nullSafeTrim(c.getContenido()), cellStyle);
        setCell(row, 2, formatFecha(c.getFecha(), dtf), cellStyle);

        setCell(row, 3, safeId(autor), cellStyle);
        setCell(row, 4, safeNombre(autor), cellStyle);
        setCell(row, 5, safeEmail(autor), cellStyle);

        setCell(row, 6, safeId(animal), cellStyle);
        setCell(row, 7, safeNombre(animal), cellStyle);

        setCell(row, 8, safeId(padre), cellStyle);
        setCell(row, 9, padre != null, cellStyle);

        setCell(row, 10, safeId(creadorAnimal), cellStyle);
        setCell(row, 11, safeNombre(creadorAnimal), cellStyle);
        setCell(row, 12, safeEmail(creadorAnimal), cellStyle);
    }

    // Métodos auxiliares
    private Long safeId(Object obj) {
        if (obj instanceof Usuario u) return u.getId();
        if (obj instanceof Animal a) return a.getId();
        if (obj instanceof Comentario c) return c.getId();
        return null;
    }

    private String safeNombre(Object obj) {
        if (obj instanceof Usuario u) return u.getNombre();
        if (obj instanceof Animal a) return a.getNombre();
        return "";
    }

    private String safeEmail(Object obj) {
        if (obj instanceof Usuario u) return u.getEmail();
        return "";
    }

    private Usuario safeGetCreador(Animal animal) {
        return animal != null ? animal.getCreador() : null;
    }

    private String formatFecha(LocalDateTime fecha, DateTimeFormatter dtf) {
        return fecha != null ? fecha.format(dtf) : "";
    }


    private void hojaPorEstadisticas(Workbook workbook,
                                     List<Comentario> comentarios,
                                     Map<String, CellStyle> estilos,
                                     DateTimeFormatter dtf) {

        CellStyle headerStyle = estilos.get(HEADER_STYLE);
        CellStyle cellStyle = estilos.get(NORMAL_STYLE);
        CellStyle percentStyle = estilos.get(PERCENT_STYLE);

        long total = comentarios.size();
        long respondidos = contarRespondidos(comentarios);
        double porcentajeRespondidos = calcularPorcentaje(respondidos, total);

        String emailTop = obtenerTopAutor(comentarios);
        String animalTop = obtenerTopAnimal(comentarios);
        String primerComentario = obtenerPrimerComentario(comentarios, dtf);
        String ultimoComentario = obtenerUltimoComentario(comentarios, dtf);
        double promedioCaracteres = calcularPromedioCaracteres(comentarios);

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
            Row row = statsSheet.createRow(i);
            Cell keyCell = row.createCell(0);
            keyCell.setCellValue(stats[i][0]);
            keyCell.setCellStyle(headerStyle);

            Cell valueCell = row.createCell(1);
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

    // Métodos auxiliares
    private long contarRespondidos(List<Comentario> comentarios) {
        return comentarios.stream().filter(c -> c.getPadre() != null).count();
    }

    private double calcularPorcentaje(long parte, long total) {
        return total > 0 ? (parte * 1.0 / total) : 0.0;
    }

    private String obtenerTopAutor(List<Comentario> comentarios) {
        return comentarios.stream()
                .filter(c -> c.getAutor() != null && c.getAutor().getEmail() != null)
                .collect(Collectors.groupingBy(c -> c.getAutor().getEmail(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + ")")
                .orElse("N/A");
    }

    private String obtenerTopAnimal(List<Comentario> comentarios) {
        return comentarios.stream()
                .filter(c -> c.getAnimal() != null && c.getAnimal().getNombre() != null)
                .collect(Collectors.groupingBy(c -> c.getAnimal().getNombre(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + " comentarios)")
                .orElse("N/A");
    }

    private String obtenerPrimerComentario(List<Comentario> comentarios, DateTimeFormatter dtf) {
        return comentarios.stream()
                .filter(c -> c.getFecha() != null)
                .min(Comparator.comparing(Comentario::getFecha))
                .map(c -> c.getFecha().format(dtf) + " (" + safeString(c.getAutor() != null ? c.getAutor().getNombre() : null) + ")")
                .orElse("N/A");
    }

    private String obtenerUltimoComentario(List<Comentario> comentarios, DateTimeFormatter dtf) {
        return comentarios.stream()
                .filter(c -> c.getFecha() != null)
                .max(Comparator.comparing(Comentario::getFecha))
                .map(c -> c.getFecha().format(dtf) + " (" + safeString(c.getAutor() != null ? c.getAutor().getNombre() : null) + ")")
                .orElse("N/A");
    }

    private double calcularPromedioCaracteres(List<Comentario> comentarios) {
        return comentarios.stream()
                .map(Comentario::getContenido)
                .filter(Objects::nonNull)
                .mapToInt(String::length)
                .average()
                .orElse(0.0);
    }


    private void hojaPorUsuario(Workbook workbook,
                                List<Comentario> comentarios,
                                Map<String, CellStyle> estilos) {

        CellStyle headerStyle = estilos.get(HEADER_STYLE);
        CellStyle cellStyle = estilos.get(NORMAL_STYLE);

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
            setCell(r, 0, safeString(u.getNombre()), cellStyle);
            setCell(r, 1, safeString(u.getEmail()), cellStyle);
            setCell(r, 2, totalByUser, cellStyle);
            setCell(r, 3, respuestasHechas, cellStyle);
            setCell(r, 4, respuestasRecibidas, cellStyle);
        }

        for (int i = 0; i < userHeaders.length; i++) userSheet.autoSizeColumn(i);
    }

    private void hojaPorAnimal(Workbook workbook,
                               List<Comentario> comentarios,
                               Map<String, CellStyle> estilos) {

        CellStyle headerStyle = estilos.get(HEADER_STYLE);
        CellStyle cellStyle = estilos.get(NORMAL_STYLE);
        CellStyle percentStyle = estilos.get(PERCENT_STYLE);

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
            setCell(r, 0, safeString(a != null ? a.getNombre() : ""), cellStyle);
            setCell(r, 1, totalAnimal, cellStyle);
            setCell(r, 2, usuariosDistintos, cellStyle);

            // Porcentaje como número con formato
            Cell pctCell = r.createCell(3);
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
        estilos.put(HEADER_STYLE, headerStyle);

        // normal
        CellStyle normal = workbook.createCellStyle();
        setBorders(normal);
        normal.setWrapText(true);
        estilos.put(NORMAL_STYLE, normal);

        // percent
        CellStyle percent = workbook.createCellStyle();
        percent.cloneStyleFrom(normal);
        DataFormat df = workbook.createDataFormat();
        percent.setDataFormat(df.getFormat("0.00%"));
        estilos.put(PERCENT_STYLE, percent);

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
        } else if (value instanceof Number numberValue) {
            cell.setCellValue(numberValue.doubleValue());
        } else if (value instanceof Boolean booleanValue) {
            cell.setCellValue(booleanValue);
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
}
