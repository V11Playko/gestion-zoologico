package com.playko.messaging.service.controller;

import com.playko.messaging.service.dto.SendNotification;
import com.playko.messaging.service.service.INotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final INotificationService emailService;

    @PostMapping("/send")
    public ResponseEntity<String> sendNotification(@Valid @RequestBody SendNotification notification) {
        emailService.sendNotification(notification);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Correo enviado correctamente a " + notification.getTo());
    }
}