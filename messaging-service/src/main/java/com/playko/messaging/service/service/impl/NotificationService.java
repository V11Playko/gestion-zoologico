package com.playko.messaging.service.service.impl;

import com.playko.messaging.service.dto.SendNotification;
import com.playko.messaging.service.exception.MessageNotSendException;
import com.playko.messaging.service.service.INotificationService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


@Service
@RequiredArgsConstructor
public class NotificationService implements INotificationService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Async
    @Override
    public void sendNotification(SendNotification notification) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(notification.getTo());
            helper.setSubject(notification.getSubject());

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
            javaMailSender.send(message);

        } catch (Exception e) {
            throw new MessageNotSendException();
        }
    }
}
