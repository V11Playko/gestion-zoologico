package com.playko.messaging.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.messaging.service.controller.NotificationController;
import com.playko.messaging.service.dto.SendNotification;
import com.playko.messaging.service.service.INotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private INotificationService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void sendNotification_success() throws Exception {
        // Arrange
        SendNotification notification = new SendNotification("test@example.com", "Hola mundo", "Este es el cuerpo");

        // Act & Assert
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isOk())
                .andExpect(content().string("Correo enviado correctamente a test@example.com"));

        verify(emailService, times(1)).sendNotification(notification);
    }

    @Test
    void sendNotification_validationError() throws Exception {
        // Arrange → el campo "to" es obligatorio, simulamos que falta
        SendNotification notification = new SendNotification("", "Asunto", "Cuerpo");

        // Act & Assert
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isBadRequest()); // Spring devuelve 400 automáticamente
        // Aquí no debe llamarse el servicio
        verify(emailService, times(0)).sendNotification(any());
    }

    @Test
    void sendNotification_serviceThrowsException() throws Exception {
        // Arrange
        SendNotification notification = new SendNotification("fail@example.com", "Asunto", "Cuerpo");

        doThrow(new RuntimeException("Error al enviar correo"))
                .when(emailService).sendNotification(notification);

        // Act & Assert
        mockMvc.perform(post("/api/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notification)))
                .andExpect(status().isInternalServerError());
        // ⚠️ Ojo: ahora mismo tu controller no captura excepciones, así que esto depende de cómo manejes los errores globalmente (ControllerAdvice).

        verify(emailService, times(1)).sendNotification(notification);
    }
}