package com.playko.zoologico.configuration.security.exception;

public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }
}
