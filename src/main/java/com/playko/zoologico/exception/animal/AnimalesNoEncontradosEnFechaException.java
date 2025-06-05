package com.playko.zoologico.exception.animal;

import java.time.LocalDate;

public class AnimalesNoEncontradosEnFechaException extends RuntimeException {
    public AnimalesNoEncontradosEnFechaException(LocalDate fecha) {
        super("No se encontraron animales registrados en la fecha: " + fecha);
    }
}