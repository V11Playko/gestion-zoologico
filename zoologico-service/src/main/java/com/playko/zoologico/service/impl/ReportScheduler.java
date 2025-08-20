package com.playko.zoologico.service.impl;

import com.playko.zoologico.service.IComentarioService;
import com.playko.zoologico.service.IExcelService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final IExcelService excelService;
    private final PdfService pdfService;
    private final S3Service s3Service;

    private static final ZoneId ZONE = ZoneId.of("America/Bogota");

    @Value("${scheduler.excel-cron}")
    private String excelCron;

    @Value("${scheduler.pdf-cron}")
    private String pdfCron;

    // EXCEL
    @Scheduled(cron = "${scheduler.excel-cron}", zone = "America/Bogota")
    public void generarYEnviarReporteExcel() {
        try {
            byte[] excel = excelService.generarExcelComentariosPorFecha(null);
            String key = "reportes/excel/comentarios-" + LocalDate.now(ZONE) + ".xlsx";
            s3Service.subirArchivo(excel, key);
            log.info("📤 Reporte Excel subido a S3 con key: {}", key);
        } catch (Exception ex) {
            log.error("Error generando/enviando reporte Excel: {}", ex.getMessage(), ex);
        }
    }

    // PDF
    @Scheduled(cron = "${scheduler.pdf-cron}", zone = "America/Bogota")
    public void generarYEnviarReportesPdf() {
        LocalDate fecha = LocalDate.now(ZONE);
        generarYEnviarReportesPdfParaFecha(fecha);
    }

    public void generarYEnviarReportesPdfParaFecha(LocalDate fecha) {
        Map<String, PdfService.PdfInfo> pdfs = pdfService.generarReportesEnMemoria(fecha);

        if (pdfs == null || pdfs.isEmpty()) {
            log.info("No se generaron PDFs para la fecha {}", fecha);
            return;
        }

        for (Map.Entry<String, PdfService.PdfInfo> e : pdfs.entrySet()) {
            String fileName = e.getKey();
            byte[] content = e.getValue().getBytes();
            String creatorEmail = e.getValue().getCreatorEmail();
            String s3Key = "reportes/pdf/" + fecha.toString() + "/" + fileName;

            try {
                Map<String, String> metadata = new HashMap<>();
                if (creatorEmail != null && !creatorEmail.isBlank()) {
                    metadata.put("creator-email", creatorEmail);
                }

                s3Service.subirArchivo(content, s3Key, "application/pdf", metadata);
                log.info("📤 PDF subido a S3: {} (creator-email={})", s3Key, creatorEmail);
            } catch (Exception ex) {
                log.error("Error subiendo a S3 el PDF {}: {}", fileName, ex.getMessage(), ex);
            }
        }
    }
}