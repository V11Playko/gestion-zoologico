package com.playko.messaging.service.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.messaging.service.dto.SendNotification;
import com.playko.messaging.service.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessor {

    private final SqsClient sqsClient;
    private final S3Client s3Client;
    private final INotificationService emailService;

    @Value("${sqs.excel-dlq-url}")
    private String excelDlqUrl;
    @Value("${sqs.pdf-dlq-url}")
    private String pdfDlqUrl;

    // ThreadLocal para recordar tipo actual (excel/pdf)
    private final ThreadLocal<String> tipoProcesando = new ThreadLocal<>();

    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void procesarMensaje(String body) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(body);
        JsonNode recordNode = root.path("Records").get(0);
        String bucket = recordNode.path("s3").path("bucket").path("name").asText();
        String key = recordNode.path("s3").path("object").path("key").asText();

        log.info("📂 Procesando S3 notification: bucket={}, key={}", bucket, key);

        String lowerKey = key.toLowerCase(Locale.ROOT);
        boolean isExcel = lowerKey.endsWith(".xlsx") || lowerKey.endsWith(".xls");
        boolean isPdf = lowerKey.endsWith(".pdf");

        if (isExcel) tipoProcesando.set("excel");
        else if (isPdf) tipoProcesando.set("pdf");
        else tipoProcesando.set("otro");

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (ResponseInputStream<GetObjectResponse> response = s3Client.getObject(getRequest);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            GetObjectResponse getObjectResponse = response.response();
            Map<String, String> metadata = getObjectResponse.metadata();
            String creatorEmail = (metadata != null && metadata.containsKey("creator-email"))
                    ? metadata.get("creator-email")
                    : null;

            response.transferTo(out);
            byte[] contenido = out.toByteArray();
            String nombreArchivo = key.substring(key.lastIndexOf('/') + 1);

            if (isExcel) {
                String destino = creatorEmail != null ? creatorEmail : "heinnervega20@gmail.com";
                log.info("📊 Excel descargado ({} bytes). Enviando a: {}", contenido.length, destino);

                SendNotification notification = SendNotification.builder()
                        .to(destino)
                        .subject("Nuevo Reporte XLSX disponible")
                        .body("Se ha generado un nuevo reporte: " + key)
                        .attachment(contenido)
                        .attachmentName(nombreArchivo)
                        .build();
                emailService.sendNotification(notification);
                log.info("📧 Notificación Excel enviada a {}", destino);

            } else if (isPdf) {
                String destino = creatorEmail != null ? creatorEmail : "heinnervega20@gmail.com";
                log.info("📄 PDF descargado ({} bytes). Enviando a: {}", contenido.length, destino);

                SendNotification notification = SendNotification.builder()
                        .to(destino)
                        .subject("Nuevo Reporte PDF disponible")
                        .body("Se ha generado un nuevo PDF: " + key)
                        .attachment(contenido)
                        .attachmentName(nombreArchivo)
                        .build();
                emailService.sendNotification(notification);
                log.info("📧 Notificación PDF enviada a {}", destino);

            } else {
                log.warn("⚠️ Tipo de archivo no manejado: {}", key);
            }
        }
    }

    @Recover
    public void recover(Exception e, String body) {
        String tipo = tipoProcesando.get();
        String targetDlq = excelDlqUrl; // default

        if ("pdf".equals(tipo)) {
            targetDlq = pdfDlqUrl;
        } else if ("excel".equals(tipo)) {
            targetDlq = excelDlqUrl;
        }

        log.error("❌ No se pudo procesar el mensaje después de varios intentos. Enviando a DLQ [{}]...", targetDlq, e);

        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(targetDlq)
                .messageBody(body)
                .build());

        tipoProcesando.remove();
    }
}