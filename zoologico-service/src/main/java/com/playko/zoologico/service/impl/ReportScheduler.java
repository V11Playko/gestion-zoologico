package com.playko.zoologico.service.impl;

import com.playko.zoologico.service.IComentarioService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class ReportScheduler {

    private final IComentarioService comentarioService;
    private final S3Service s3Service;

    @Scheduled(cron = "0 32 17 * * *", zone = "America/Bogota")
    public void generarYEnviarReporte() {
        byte[] excel = comentarioService.generarExcelComentariosPorFecha(null);

        String key = "reportes/comentarios-" + LocalDate.now() + ".xlsx";

        String s3Key = s3Service.subirArchivo(excel, key);

        System.out.println("📤 Reporte subido a S3 con key: " + s3Key);
    }
}
