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
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProcessor {

    private final SqsClient sqsClient;
    private final S3Client s3Client;
    private final INotificationService emailService;

    @Value("${sqs.dlq-url}")
    private String dlqUrl;

    @Retryable(
            value = { Exception.class },
            maxAttempts = 3,  // opcional: controla cuántos intentos
            backoff = @Backoff(delay = 2000, multiplier = 2) // 2s, 4s, 8s
    )
    public void procesarMensaje(String body) throws Exception {
        log.debug("📜 Cuerpo del mensaje: {}", body);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(body);

        JsonNode recordNode = root.path("Records").get(0);
        String bucket = recordNode.path("s3").path("bucket").path("name").asText();
        String key = recordNode.path("s3").path("object").path("key").asText();

        log.info("📂 Obteniendo archivo de S3 - Bucket: {}, Key: {}", bucket, key);

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        try (InputStream in = s3Client.getObject(getRequest);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            in.transferTo(out);
            byte[] excel = out.toByteArray();

            log.info("📊 Archivo descargado correctamente ({} bytes)", excel.length);

            SendNotification notification = SendNotification.builder()
                    .to("heinnervega20@gmail.com")
                    .subject("Nuevo Reporte Disponible")
                    .body("Se ha generado un nuevo reporte: " + key)
                    .attachment(excel)
                    .attachmentName(key.substring(key.lastIndexOf("/") + 1))
                    .build();

            emailService.sendNotification(notification);
            log.info("📧 Notificación enviada correctamente para el archivo {}", key);
        }
    }

    @Recover
    public void recover(Exception e, String body) {
        log.error("❌ No se pudo procesar el mensaje después de varios intentos. Enviando a DLQ...", e);
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(dlqUrl)
                .messageBody(body)
                .build());
    }
}
