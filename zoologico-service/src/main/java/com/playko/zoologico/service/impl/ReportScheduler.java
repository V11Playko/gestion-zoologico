package com.playko.zoologico.service.impl;

import com.playko.zoologico.service.IComentarioService;
import com.playko.zoologico.service.IExcelService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ReportScheduler {

    private final IExcelService excelService;
    private final S3Service s3Service;

    @Scheduled(cron = "0 32 17 * * *", zone = "America/Bogota")
    public void generarYEnviarReporte() {
        byte[] excel = excelService.generarExcelComentariosPorFecha(null);

        String key = "reportes/comentarios-" + LocalDate.now() + ".xlsx";

        String s3Key = s3Service.subirArchivo(excel, key);

        // Reemplazamos System.out por log
        log.info("📤 Reporte subido a S3 con key: {}", s3Key);
    }
}
