package com.playko.messaging.service.configuration;

import com.playko.messaging.service.exception.MessageNotSendException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerAdvisorTest {

    private ControllerAdvisor controllerAdvisor;

    @BeforeEach
    void setUp() {
        controllerAdvisor = new ControllerAdvisor();
    }

    @Test
    void handleConstraintViolationException_returnsBadRequest() {
        // Mock ConstraintViolation
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("fieldName");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must not be null");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<Map<String, String>> response = controllerAdvisor.handleConstraintViolationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("must not be null", response.getBody().get("fieldName"));
    }

    @Test
    void handleBindExceptions_returnsBadRequest() {
        // Crear BindException con FieldError
        BindException bindException = mock(BindException.class);
        FieldError fieldError = new FieldError("objectName", "fieldName", "cannot be empty");
        when(bindException.getBindingResult()).thenReturn(mock(org.springframework.validation.BindingResult.class));
        when(bindException.getBindingResult().getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Map<String, String>> response = controllerAdvisor.handleBindExceptions(bindException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("cannot be empty", response.getBody().get("fieldName"));
    }

    @Test
    void handleMessageNotSendException_returnsBadRequestWithMessage() {
        MessageNotSendException ex = new MessageNotSendException();

        ResponseEntity<Map<String, String>> response = controllerAdvisor.handleMessageNotSendException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertTrue(response.getBody().containsKey("Mensaje"));
        assertEquals("El mensaje no se envio correctamente.", response.getBody().get("Mensaje"));
    }
}