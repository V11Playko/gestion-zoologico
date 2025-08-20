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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SqsListenerService {

    private final SqsClient sqsClient;
    private final MessageProcessor messageProcessor;

    @Value("${sqs.excel-queue-url}")
    private String excelQueueUrl;

    @Value("${sqs.pdf-queue-url}")
    private String pdfQueueUrl;

    @Scheduled(fixedDelay = 10000)
    public void escucharMensajesExcel() {
        log.info("🔄 Iniciando polling de la cola Excel...");

        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(excelQueueUrl)
                    .waitTimeSeconds(20)
                    .maxNumberOfMessages(5)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();
            log.info("📩 Se recibieron {} mensajes de la cola Excel", messages.size());

            for (Message msg : messages) {
                log.info("➡️ Procesando mensaje Excel con ID: {}", msg.messageId());

                messageProcessor.procesarMensaje(msg.body());

                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(excelQueueUrl)
                        .receiptHandle(msg.receiptHandle())
                        .build());

                log.info("✅ Mensaje Excel {} eliminado de la cola", msg.messageId());
            }

        } catch (Exception e) {
            log.error("❌ Error al escuchar mensajes de la cola Excel", e);
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void escucharMensajesPdf() {
        log.info("🔄 Iniciando polling de la cola PDF...");

        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(pdfQueueUrl)
                    .waitTimeSeconds(20)
                    .maxNumberOfMessages(5)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();
            log.info("📩 Se recibieron {} mensajes de la cola PDF", messages.size());

            for (Message msg : messages) {
                log.info("➡️ Procesando mensaje PDF con ID: {}", msg.messageId());

                messageProcessor.procesarMensaje(msg.body());

                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(pdfQueueUrl)
                        .receiptHandle(msg.receiptHandle())
                        .build());

                log.info("✅ Mensaje PDF {} eliminado de la cola", msg.messageId());
            }

        } catch (Exception e) {
            log.error("❌ Error al escuchar mensajes de la cola PDF", e);
        }
    }
}
