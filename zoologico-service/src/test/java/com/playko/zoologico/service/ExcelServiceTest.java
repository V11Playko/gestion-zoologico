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

    /* ===== fechaStr nulo/vacío — rama else del parsing ===== */

    @Test
    void generarExcelComentariosPorFecha_shouldUseCurrentDate_whenFechaIsNull() {
        when(comentarioRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
        // Sin comentarios -> NoComentariosEnFechaException; confirma que se llegó al else branch
        assertThrows(NoComentariosEnFechaException.class,
                () -> excelService.generarExcelComentariosPorFecha(null));
    }

    @Test
    void generarExcelComentariosPorFecha_shouldUseCurrentDate_whenFechaIsEmpty() {
        when(comentarioRepository.findByFechaBetween(any(), any())).thenReturn(List.of());
        assertThrows(NoComentariosEnFechaException.class,
                () -> excelService.generarExcelComentariosPorFecha(""));
    }

    /* ===== Comentario completamente poblado — cubre hojaPorUsuario y hojaPorAnimal ===== */

    @Test
    void generarExcelComentariosPorFecha_shouldGenerateUserAndAnimalSheets_withFullData() throws Exception {
        Usuario autorFull = new Usuario();
        autorFull.setId(1L);
        autorFull.setNombre("Autor Full");
        autorFull.setEmail("autor@test.com");

        Usuario creadorFull = new Usuario();
        creadorFull.setId(2L);
        creadorFull.setNombre("Creador");
        creadorFull.setEmail("creador@test.com");

        Animal animalFull = new Animal();
        animalFull.setId(10L);
        animalFull.setNombre("Animal Full");
        animalFull.setCreatedBy(creadorFull);

        LocalDateTime fechaHora = LocalDate.of(2023, 8, 1).atTime(9, 0);

        // Comentario padre
        Comentario padre = new Comentario();
        padre.setId(1L);
        padre.setContenido("Comentario padre");
        padre.setFecha(fechaHora);
        padre.setAutor(autorFull);
        padre.setAnimal(animalFull);
        padre.setPadre(null);

        // Respuesta al padre — ejercita safeId(Comentario) y rama padre != null
        Comentario hijo = new Comentario();
        hijo.setId(2L);
        hijo.setContenido("Respuesta");
        hijo.setFecha(fechaHora.plusMinutes(30));
        hijo.setAutor(autorFull);
        hijo.setAnimal(animalFull);
        hijo.setPadre(padre);

        when(comentarioRepository.findByFechaBetween(any(), any())).thenReturn(List.of(padre, hijo));

        byte[] bytes = excelService.generarExcelComentariosPorFecha("2023-08-01");

        assertThat(bytes).isNotNull().hasSizeGreaterThan(0);
        try (Workbook wb = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            // Hoja "Por Usuario" debe tener al menos 1 fila de datos
            Sheet userSheet = wb.getSheet("Por Usuario");
            assertThat(userSheet).isNotNull();
            assertThat(userSheet.getPhysicalNumberOfRows()).isGreaterThan(1);

            // Hoja "Por Animal" debe tener al menos 1 fila de datos
            Sheet animalSheet = wb.getSheet("Por Animal");
            assertThat(animalSheet).isNotNull();
            assertThat(animalSheet.getPhysicalNumberOfRows()).isGreaterThan(1);
        }
    }

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