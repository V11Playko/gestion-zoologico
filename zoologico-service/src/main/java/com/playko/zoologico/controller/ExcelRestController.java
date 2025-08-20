package com.playko.zoologico.controller;

import com.playko.zoologico.service.IExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

@RestController
@RequestMapping("/api/reporte")
@RequiredArgsConstructor
public class ExcelRestController {

    private final IExcelService excelService;


    @GetMapping("/excel")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<byte[]> generarExcelComentariosPorFecha(
            @RequestParam(required = false) String fecha) {

        byte[] fileBytes = excelService.generarExcelComentariosPorFecha(fecha);

        LocalDate dateForName;
        try {
            dateForName = (fecha != null) ? LocalDate.parse(fecha) : LocalDate.now(ZoneId.of("America/Bogota"));
        } catch (DateTimeParseException e) {
            dateForName = LocalDate.now(ZoneId.of("America/Bogota"));
        }

        String filename = "reporte-comentarios-" + dateForName.toString() + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");

        return ResponseEntity.status(HttpStatus.CREATED)
                .headers(headers)
                .body(fileBytes);
    }

}
