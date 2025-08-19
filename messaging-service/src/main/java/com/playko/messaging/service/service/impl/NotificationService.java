package com.playko.messaging.service.service.impl;

import com.playko.messaging.service.dto.EmailLog;
import com.playko.messaging.service.dto.SendNotification;
import com.playko.messaging.service.exception.MessageNotSendException;
import com.playko.messaging.service.repository.EmailLogRepository;
import com.playko.messaging.service.service.INotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;
    private final EmailLogRepository emailLogRepository;

    @Async
    @Override
    public void sendNotification(SendNotification notification) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getTo());
            helper.setSubject(notification.getSubject());

            // Procesar template con Thymeleaf
            Context context = new Context();
            context.setVariable("subject", notification.getSubject());
            context.setVariable("body", notification.getBody());
            context.setVariable("animalName", notification.getAnimalName());
            context.setVariable("commentAuthorName", notification.getCommentAuthorName());
            context.setVariable("commentDate", notification.getCommentDate());
            context.setVariable("animalId", notification.getAnimalId());
            context.setVariable("commentId", notification.getCommentId());
            context.setVariable("commentAuthorEmail", notification.getCommentAuthorEmail());

            String contenidoHtml = templateEngine.process("email", context);
            helper.setText(contenidoHtml, true);

            // 🔹 Agregar adjunto si existe
            if (notification.getAttachment() != null && notification.getAttachmentName() != null) {
                helper.addAttachment(notification.getAttachmentName(),
                        () -> new ByteArrayInputStream(notification.getAttachment()));
            }

            javaMailSender.send(message);

            // Obtener Message-ID
            String[] messageIds = message.getHeader("Message-ID");
            String messageId = (messageIds != null && messageIds.length > 0) ? messageIds[0] : null;

            // Guardar en MongoDB
            EmailLog log = EmailLog.builder()
                    .messageId(messageId)
                    .to(notification.getTo())
                    .subject(notification.getSubject())
                    .body(notification.getBody())
                    .commentAuthorName(notification.getCommentAuthorName())
                    .commentAuthorEmail(notification.getCommentAuthorEmail())
                    .animalName(notification.getAnimalName())
                    .animalId(notification.getAnimalId())
                    .commentId(notification.getCommentId())
                    .sentAt(LocalDateTime.now())
                    .build();

            emailLogRepository.save(log);
        } catch (Exception e) {
            throw new MessageNotSendException();
        }
    }
}
