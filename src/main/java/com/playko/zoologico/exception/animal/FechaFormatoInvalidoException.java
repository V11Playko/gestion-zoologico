package com.playko.zoologico.exception.animal;

public class FechaFormatoInvalidoException extends RuntimeException {
    public FechaFormatoInvalidoException(String fecha) {
        super("Formato de fecha inválido: '" + fecha + "'. El formato correcto es 'yyyy-MM-dd'.");
    }
}
