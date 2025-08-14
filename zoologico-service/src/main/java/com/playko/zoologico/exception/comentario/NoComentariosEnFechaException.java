package com.playko.zoologico.exception.comentario;

import java.time.LocalDate;

import static com.playko.zoologico.constants.ExceptionMessages.NO_COMENTARIOS_EN_FECHA_MESSAGE;

public class NoComentariosEnFechaException extends RuntimeException {

    public NoComentariosEnFechaException(LocalDate fecha) {
        super(NO_COMENTARIOS_EN_FECHA_MESSAGE + fecha);
    }
}
