package com.playko.zoologico.advice;

import com.playko.zoologico.exception.ErrorGeneratingExcelException;
import com.playko.zoologico.exception.ErrorGettingMailTokenException;
import com.playko.zoologico.exception.FechaFormatoInvalidoException;
import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.NonNegativePageNumberException;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.animal.AnimalSinComentariosException;
import com.playko.zoologico.exception.animal.AnimalesNoEncontradosEnFechaException;
import com.playko.zoologico.exception.animal.ZonaConAnimalesException;
import com.playko.zoologico.exception.comentario.ComentarioAnimalMismatchException;
import com.playko.zoologico.exception.comentario.ComentarioPadreNotFoundException;
import com.playko.zoologico.exception.comentario.NoComentariosEnFechaException;
import com.playko.zoologico.exception.especie.EspecieAlreadyExistsException;
import com.playko.zoologico.exception.especie.EspecieConAnimalesException;
import com.playko.zoologico.exception.especie.EspecieNotFoundException;
import com.playko.zoologico.exception.usuario.EmailAlreadyExistsException;
import com.playko.zoologico.exception.usuario.RoleNotFoundException;
import com.playko.zoologico.exception.usuario.UsuarioNotFoundException;
import com.playko.zoologico.exception.zona.IdZonaInvalidException;
import com.playko.zoologico.exception.zona.ZonaAlreadyExistsException;
import com.playko.zoologico.exception.zona.ZonaEspecieMismatchException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.playko.zoologico.constants.ExceptionMessages.ANIMAL_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ANIMAL_SIN_COMENTARIOS_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.COMENTARIO_ANIMAL_MISMATCH_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.COMENTARIO_PADRE_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.EMAIL_ALREADY_EXISTS_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ERROR_GENERATING_EXCEL_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ERROR_GETTING_MAIL_TOKEN_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ESPECIE_ALREADY_EXISTS_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ESPECIE_CON_ANIMALES_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ESPECIE_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ID_ZONA_INVALID_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.NON_NEGATIVE_PAGE_NUMBER_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.NO_DATA_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ROLE_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.USER_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ZONA_ALREADY_EXISTS;
import static com.playko.zoologico.constants.ExceptionMessages.ZONA_CON_ANIMALES_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ZONA_ESPECIE_MISMATCH_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ZONA_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.GlobalConstants.RESPONSE_MESSAGE_KEY;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerAdvisorTest {

    private ControllerAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new ControllerAdvisor();
    }

    // -----------------------------
    // ConstraintViolationException
    // -----------------------------
    @Test
    void handleConstraintViolationException_returnsBadRequestAndMap() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);

        when(path.toString()).thenReturn("miCampo");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("no puede ser nulo");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException(violations);

        ResponseEntity<Map<String, String>> response = advisor.handleConstraintViolationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("no puede ser nulo", response.getBody().get("miCampo"));
    }

    // -------------------------------------------
    // MethodArgumentNotValidException (validation)
    // -------------------------------------------
    @Test
    void handleValidationExceptions_returnsBadRequestAndMap() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "target");
        FieldError fe = new FieldError("target", "campo", "debe existir");
        bindingResult.addError(fe);

        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> response = advisor.handleValidationExceptions(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("debe existir", response.getBody().get("campo"));
    }

    // -----------------------------
    // BindException
    // -----------------------------
    @Test
    void handleBindExceptions_returnsBadRequestAndMap() throws Exception {
        BindException bindException = new BindException(new Object(), "obj");
        bindException.addError(new FieldError("obj", "campoBind", "no valido"));

        ResponseEntity<Map<String, String>> response = advisor.handleBindExceptions(bindException);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("no valido", response.getBody().get("campoBind"));
    }

    // -----------------------------------------
    // Handlers que devuelven singletonMap con constantes
    // -----------------------------------------

    @Test
    void handleNoDataFoundException_returnsNotFoundWithConfiguredMessage() {
        NoDataFoundException ex = new NoDataFoundException();
        ResponseEntity<Map<String, String>> response = advisor.handleNoDataFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = NO_DATA_FOUND_MESSAGE;
        assertEquals(1, response.getBody().size());
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleZonaNotFoundException_returnsNotFoundWithConfiguredMessage() {
        ZonaNotFoundException ex = new ZonaNotFoundException();
        ResponseEntity<Map<String, String>> response = advisor.handleZonaNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ZONA_NOT_FOUND_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleZonaAlreadyExistsException_returnsBadRequestWithConfiguredMessage() {
        ZonaAlreadyExistsException ex = new ZonaAlreadyExistsException();
        ResponseEntity<Map<String, String>> response = advisor.handleZonaAlreadyExistsException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ZONA_ALREADY_EXISTS;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleEspecieAlreadyExistsException_returnsBadRequestWithConfiguredMessage() {
        EspecieAlreadyExistsException ex = new EspecieAlreadyExistsException();
        ResponseEntity<Map<String, String>> response = advisor.handleEspecieAlreadyExistsException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ESPECIE_ALREADY_EXISTS_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleEspecieConAnimalesException_returnsBadRequestWithConfiguredMessage() {
        EspecieConAnimalesException ex = new EspecieConAnimalesException();
        ResponseEntity<Map<String, String>> response = advisor.handleEspecieConAnimalesException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ESPECIE_CON_ANIMALES_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleEspecieNotFoundException_returnsNotFoundWithConfiguredMessage() {
        EspecieNotFoundException ex = new EspecieNotFoundException();
        ResponseEntity<Map<String, String>> response = advisor.handleEspecieNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ESPECIE_NOT_FOUND_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleAnimalNotFoundException_returnsNotFoundWithConfiguredMessage() {
        AnimalNotFoundException ex = new AnimalNotFoundException();
        ResponseEntity<Map<String, String>> response = advisor.handleAnimalNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ANIMAL_NOT_FOUND_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleEmailAlreadyExistsException_returnsBadRequestWithConfiguredMessage() {
        EmailAlreadyExistsException ex = new EmailAlreadyExistsException();
        ResponseEntity<Map<String, String>> response = advisor.handleEmailAlreadyExistsException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = EMAIL_ALREADY_EXISTS_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleRoleNotFoundException_returnsNotFoundWithConfiguredMessage() {
        RoleNotFoundException ex = new RoleNotFoundException();
        ResponseEntity<Map<String, String>> response = advisor.handleRoleNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ROLE_NOT_FOUND_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleComentarioPadreNotFoundException_returnsNotFoundWithConfiguredMessage() {
        ComentarioPadreNotFoundException ex = new ComentarioPadreNotFoundException();
        ResponseEntity<Map<String, String>> response = advisor.handleComentarioPadreNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = COMENTARIO_PADRE_NOT_FOUND_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleZonaConAnimalesException_returnsBadRequestWithConfiguredMessage() {
        ZonaConAnimalesException ex = new ZonaConAnimalesException();
        ResponseEntity<Map<String, String>> response = advisor.handleZonaConAnimalesException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ZONA_CON_ANIMALES_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleUsuarioNotFoundException_returnsNotFoundWithConfiguredMessage() {
        UsuarioNotFoundException ex = new UsuarioNotFoundException();
        ResponseEntity<Map<String, String>> response = advisor.handleUsuarioNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = USER_NOT_FOUND_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleZonaEspecieMismatchException_returnsBadRequestWithConfiguredMessage() {
        ZonaEspecieMismatchException ex = new ZonaEspecieMismatchException();
        ResponseEntity<Map<String, String>> response = advisor.handleZonaEspecieMismatchException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ZONA_ESPECIE_MISMATCH_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleAnimalSinComentariosException_returnsBadRequestWithConfiguredMessage() {
        AnimalSinComentariosException ex = new AnimalSinComentariosException();
        ResponseEntity<Map<String, String>> response = advisor.handleAnimalSinComentariosException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ANIMAL_SIN_COMENTARIOS_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleComentarioAnimalMismatchException_returnsBadRequestWithConfiguredMessage() {
        ComentarioAnimalMismatchException ex = new ComentarioAnimalMismatchException();
        ResponseEntity<Map<String, String>> response = advisor.handleComentarioAnimalMismatchException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = COMENTARIO_ANIMAL_MISMATCH_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleNonNegativePageNumberException_returnsBadRequestWithConfiguredMessage() {
        NonNegativePageNumberException ex = new NonNegativePageNumberException();
        ResponseEntity<Map<String, String>> response = advisor.handleNonNegativePageNumberException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = NON_NEGATIVE_PAGE_NUMBER_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleIdZonaInvalidException_returnsBadRequestWithConfiguredMessage() {
        IdZonaInvalidException ex = new IdZonaInvalidException();
        ResponseEntity<Map<String, String>> response = advisor.handleIdZonaInvalidException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ID_ZONA_INVALID_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleErrorGettingMailTokenException_returnsBadRequestWithConfiguredMessage() {
        ErrorGettingMailTokenException ex = new ErrorGettingMailTokenException();
        ResponseEntity<Map<String, String>> response = advisor.handleErrorGettingMailTokenException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ERROR_GETTING_MAIL_TOKEN_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    @Test
    void handleErrorGeneratingExcelException_returnsBadRequestWithConfiguredMessage() {
        ErrorGeneratingExcelException ex = new ErrorGeneratingExcelException();
        ResponseEntity<Map<String, String>> response = advisor.handleErrorGeneratingExcelException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        String expected = ERROR_GENERATING_EXCEL_MESSAGE;
        assertEquals(expected, response.getBody().get(key));
    }

    // -----------------------------------
    // Handlers que devuelven ex.getMessage
    // -----------------------------------
    @Test
    void manejarFechaInvalida_returnsBadRequestWithExceptionMessage() {
        String fechaRecibida = "2025/01/01"; // fecha malformada
        FechaFormatoInvalidoException ex = new FechaFormatoInvalidoException(fechaRecibida);

        ResponseEntity<String> response = advisor.manejarFechaInvalida(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ex.getMessage(), response.getBody()); // aquí usamos el mensaje real de la excepción
    }


    @Test
    void manejarAnimalesNoEncontrados_returnsNotFoundWithExceptionMessage() {
        LocalDate fecha = LocalDate.of(2025, 1, 01);
        AnimalesNoEncontradosEnFechaException ex = new AnimalesNoEncontradosEnFechaException(fecha);

        ResponseEntity<String> response = advisor.manejarAnimalesNoEncontrados(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(ex.getMessage(), response.getBody());
    }

    @Test
    void handleNoComentariosEnFechaException_returnsNotFoundWithExceptionMessageInMap() {
        LocalDate fecha = LocalDate.of(2025, 1, 1);
        NoComentariosEnFechaException ex = new NoComentariosEnFechaException(fecha);

        ResponseEntity<Map<String, String>> response = advisor.handleNoComentariosEnFechaException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        String key = RESPONSE_MESSAGE_KEY;
        assertEquals(ex.getMessage(), response.getBody().get(key));
    }
}