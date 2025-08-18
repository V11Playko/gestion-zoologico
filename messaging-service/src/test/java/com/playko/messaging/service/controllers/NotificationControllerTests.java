package com.playko.messaging.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.playko.messaging.service.configuration.security.jwt.JwtAuthorizationFilter;
import com.playko.messaging.service.configuration.security.jwt.JwtUtils;
import com.playko.messaging.service.controller.NotificationController;
import com.playko.messaging.service.dto.SendNotification;
import com.playko.messaging.service.service.INotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import(JwtAuthorizationFilter.class) // importamos el filtro para que forme parte del contexto
class NotificationControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private INotificationService emailService;

    @MockBean
    private JwtUtils jwtUtils; // mockeamos JwtUtils para controlar validación y roles

    private static final String BASE = "/api/notifications/send";

    @Test
    @DisplayName("POST /send con token válido y rol permitido -> 200 OK")
    void sendNotification_WithValidToken_ReturnsOk() throws Exception {
        SendNotification request = new SendNotification();
        request.setTo("test@example.com");
        request.setSubject("Prueba");
        request.setBody("Mensaje de prueba");

        String token = "valid-token";

        // Preparar mocks
        when(jwtUtils.validateJwtToken(eq(token))).thenReturn(true);
        when(jwtUtils.getRoles(eq(token))).thenReturn(List.of("ROLE_ADMIN"));
        doNothing().when(emailService).sendNotification(any(SendNotification.class));

        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().string("Correo enviado correctamente a test@example.com"));
    }

    @Test
    @DisplayName("POST /send sin token -> 401 Unauthorized")
    void sendNotification_WithoutToken_ReturnsUnauthorized() throws Exception {
        SendNotification request = new SendNotification();
        request.setTo("test@example.com");
        request.setSubject("Prueba");
        request.setBody("Mensaje de prueba");

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /send con token inválido -> 401 Unauthorized")
    void sendNotification_WithInvalidToken_ReturnsUnauthorized() throws Exception {
        SendNotification request = new SendNotification();
        request.setTo("test@example.com");
        request.setSubject("Prueba");
        request.setBody("Mensaje de prueba");

        String token = "invalid-token";

        when(jwtUtils.validateJwtToken(eq(token))).thenReturn(false);

        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /send con rol no autorizado -> 401 Unauthorized")
    void sendNotification_WithUnauthorizedRole_ReturnsUnauthorized() throws Exception {
        SendNotification request = new SendNotification();
        request.setTo("test@example.com");
        request.setSubject("Prueba");
        request.setBody("Mensaje de prueba");

        String token = "valid-but-wrong-role";

        when(jwtUtils.validateJwtToken(eq(token))).thenReturn(true);
        // rol que no está en rolesEndpointsMap (ROLE_ADMIN/ROLE_EMPLEADO/ROLE_CLIENTE)
        when(jwtUtils.getRoles(eq(token))).thenReturn(List.of("ROLE_SOME_OTHER"));

        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /send con token válido pero payload inválido -> 400 Bad Request")
    void sendNotification_WithValidToken_ButInvalidPayload_ReturnsBadRequest() throws Exception {
        SendNotification request = new SendNotification();
        request.setTo(""); // inválido para forzar error de @Valid
        request.setSubject("Prueba");
        request.setBody("Mensaje de prueba");

        String token = "valid-token-for-validation";

        when(jwtUtils.validateJwtToken(eq(token))).thenReturn(true);
        when(jwtUtils.getRoles(eq(token))).thenReturn(List.of("ROLE_ADMIN"));

        mockMvc.perform(post(BASE)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}