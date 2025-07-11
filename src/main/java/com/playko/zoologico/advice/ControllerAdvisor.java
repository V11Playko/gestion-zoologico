package com.playko.zoologico.advice;

import com.playko.zoologico.exception.NoDataFoundException;
import com.playko.zoologico.exception.animal.AnimalNotFoundException;
import com.playko.zoologico.exception.animal.AnimalSinComentariosException;
import com.playko.zoologico.exception.animal.AnimalesNoEncontradosEnFechaException;
import com.playko.zoologico.exception.animal.FechaFormatoInvalidoException;
import com.playko.zoologico.exception.animal.ZonaConAnimalesException;
import com.playko.zoologico.exception.comentario.ComentarioAnimalMismatchException;
import com.playko.zoologico.exception.comentario.ComentarioPadreNotFoundException;
import com.playko.zoologico.exception.especie.EspecieAlreadyExistsException;
import com.playko.zoologico.exception.especie.EspecieConAnimalesException;
import com.playko.zoologico.exception.especie.EspecieNotFoundException;
import com.playko.zoologico.exception.usuario.EmailAlreadyExistsException;
import com.playko.zoologico.exception.usuario.RoleNotFoundException;
import com.playko.zoologico.exception.usuario.UsuarioNotFoundException;
import com.playko.zoologico.exception.zona.ZonaAlreadyExistsException;
import com.playko.zoologico.exception.zona.ZonaEspecieMismatchException;
import com.playko.zoologico.exception.zona.ZonaNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.playko.zoologico.constants.ExceptionMessages.ANIMAL_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ANIMAL_SIN_COMENTARIOS_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.COMENTARIO_ANIMAL_MISMATCH_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.COMENTARIO_PADRE_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.EMAIL_ALREADY_EXISTS_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ESPECIE_ALREADY_EXISTS_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ESPECIE_CON_ANIMALES_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ESPECIE_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.NO_DATA_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ROLE_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.USER_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ZONA_ALREADY_EXISTS;
import static com.playko.zoologico.constants.ExceptionMessages.ZONA_CON_ANIMALES_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ZONA_ESPECIE_MISMATCH_MESSAGE;
import static com.playko.zoologico.constants.ExceptionMessages.ZONA_NOT_FOUND_MESSAGE;
import static com.playko.zoologico.constants.GlobalConstants.RESPONSE_MESSAGE_KEY;

@ControllerAdvice
public class ControllerAdvisor {
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();

        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        for (ConstraintViolation<?> violation : violations) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        }

        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<Map<String, String>> handleBindExceptions(BindException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    // Excepciones

    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<Map<String, String>> handleNoDataFoundException(
            NoDataFoundException noDataFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, NO_DATA_FOUND_MESSAGE));
    }

    @ExceptionHandler(ZonaNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleZonaNotFoundException(
            ZonaNotFoundException zonaNotFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ZONA_NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(ZonaAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleZonaAlreadyExistsException(
            ZonaAlreadyExistsException zonaAlreadyExistsException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ZONA_ALREADY_EXISTS));
    }

    @ExceptionHandler(EspecieAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEspecieAlreadyExistsException(
            EspecieAlreadyExistsException especieAlreadyExistsException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ESPECIE_ALREADY_EXISTS_MESSAGE));
    }

    @ExceptionHandler(EspecieConAnimalesException.class)
    public ResponseEntity<Map<String, String>> handleEspecieConAnimalesException(
            EspecieConAnimalesException especieConAnimalesException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ESPECIE_CON_ANIMALES_MESSAGE));
    }

    @ExceptionHandler(EspecieNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEspecieNotFoundException(
            EspecieNotFoundException especieNotFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ESPECIE_NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(AnimalNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleAnimalNotFoundException(
            AnimalNotFoundException animalNotFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ANIMAL_NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleEmailAlreadyExistsException(
            EmailAlreadyExistsException emailAlreadyExistsException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, EMAIL_ALREADY_EXISTS_MESSAGE));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleRoleNotFoundException(
            RoleNotFoundException roleNotFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ROLE_NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(ComentarioPadreNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleComentarioPadreNotFoundException(
            ComentarioPadreNotFoundException comentarioPadreNotFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, COMENTARIO_PADRE_NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(ZonaConAnimalesException.class)
    public ResponseEntity<Map<String, String>> handleZonaConAnimalesException(
            ZonaConAnimalesException zonaConAnimalesException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ZONA_CON_ANIMALES_MESSAGE));
    }

    @ExceptionHandler(UsuarioNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUsuarioNotFoundException(
            UsuarioNotFoundException usuarioNotFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, USER_NOT_FOUND_MESSAGE));
    }

    @ExceptionHandler(ZonaEspecieMismatchException.class)
    public ResponseEntity<Map<String, String>> handleZonaEspecieMismatchException(
            ZonaEspecieMismatchException zonaEspecieMismatchException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ZONA_ESPECIE_MISMATCH_MESSAGE));
    }

    @ExceptionHandler(AnimalSinComentariosException.class)
    public ResponseEntity<Map<String, String>> handleAnimalSinComentariosException(
            AnimalSinComentariosException animalSinComentariosException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, ANIMAL_SIN_COMENTARIOS_MESSAGE));
    }

    @ExceptionHandler(ComentarioAnimalMismatchException.class)
    public ResponseEntity<Map<String, String>> handleComentarioAnimalMismatchException(
            ComentarioAnimalMismatchException comentarioAnimalMismatchException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap(RESPONSE_MESSAGE_KEY, COMENTARIO_ANIMAL_MISMATCH_MESSAGE));
    }
    @ExceptionHandler(FechaFormatoInvalidoException.class)
    public ResponseEntity<String> manejarFechaInvalida(FechaFormatoInvalidoException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(AnimalesNoEncontradosEnFechaException.class)
    public ResponseEntity<String> manejarAnimalesNoEncontrados(AnimalesNoEncontradosEnFechaException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
}
