package com.playko.zoologico.service;

import com.playko.zoologico.entity.Animal;
import com.playko.zoologico.entity.Comentario;
import com.playko.zoologico.entity.Usuario;
import com.playko.zoologico.exception.FechaFormatoInvalidoException;
import com.playko.zoologico.exception.comentario.NoComentariosEnFechaException;
import com.playko.zoologico.repository.IComentarioRepository;
import com.playko.zoologico.service.impl.ExcelService;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExcelServiceTest {

    @Mock
    private IComentarioRepository comentarioRepository;

    @InjectMocks
    private ExcelService excelService;

    private Usuario autor;
    private Animal animal;

    /* ===== generarExcelComentariosPorFecha ===== */

    @Test
    void generarExcelComentariosPorFecha_shouldThrow_whenFechaFormatoInvalido() {
        String bad = "2023-99-99";
        assertThrows(FechaFormatoInvalidoException.class, () -> excelService.generarExcelComentariosPorFecha(bad));
    }

    @Test
    void generarExcelComentariosPorFecha_shouldThrow_whenNoComments() {
        // make repository return empty for any start/end
        when(comentarioRepository.findByFechaBetween(any(), any())).thenReturn(List.of());

        assertThrows(NoComentariosEnFechaException.class, () -> excelService.generarExcelComentariosPorFecha("2023-08-01"));
    }

    @Test
    void generarExcelComentariosPorFecha_shouldReturnByteArray_andContainExpectedSheets() throws Exception {
        // Preparar un comentario en una fecha específica
        LocalDateTime fechaHora = LocalDate.of(2023, 8, 1).atTime(10, 30);
        Comentario c1 = new Comentario();
        c1.setId(500L);
        c1.setContenido("Contenido prueba");
        c1.setFecha(fechaHora);
        c1.setAutor(autor);
        c1.setAnimal(animal);

        // Mock repository
        when(comentarioRepository.findByFechaBetween(any(), any())).thenReturn(List.of(c1));

        byte[] bytes = excelService.generarExcelComentariosPorFecha("2023-08-01");

        // Verificar resultado básico
        assertThat(bytes)
                .isNotNull()
                .hasSizeGreaterThan(0);

        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            // Convertir hojas a lista
            List<Sheet> sheets = new ArrayList<>();
            for (int i = 0; i < wb.getNumberOfSheets(); i++) {
                sheets.add(wb.getSheetAt(i));
            }

            // Verificar cantidad mínima de hojas
            assertThat(sheets).hasSizeGreaterThanOrEqualTo(4);

            // Verificar que todas las hojas esperadas existan en una sola cadena de aserciones
            assertThat(List.of(
                    wb.getSheet("Comentarios-2023-08-01"),
                    wb.getSheet("Estadísticas"),
                    wb.getSheet("Por Usuario"),
                    wb.getSheet("Por Animal")
            )).allMatch(Objects::nonNull, "Todas las hojas esperadas deben existir");
        }
    }

}