package com.playko.zoologico.exception;


import static com.playko.zoologico.constants.ExceptionMessages.FECHA_INVALIDA;

public class FechaFormatoInvalidoException extends RuntimeException {
    public FechaFormatoInvalidoException(String fecha) {
        super(FECHA_INVALIDA + " Recibido: '" + fecha + "'");
    }
}
