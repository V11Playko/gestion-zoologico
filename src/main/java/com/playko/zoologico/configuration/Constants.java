package com.playko.zoologico.configuration;

public class Constants {
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

    public static final String RESPONSE_MESSAGE_KEY = "Mensaje";

    /**
     * Mensajes de Swagger
     */

    public static final String SWAGGER_TITLE_MESSAGE = "User API";
    public static final String SWAGGER_DESCRIPTION_MESSAGE = "User microservice";
    public static final String SWAGGER_VERSION_MESSAGE = "1.0.0";
    public static final String SWAGGER_LICENSE_NAME_MESSAGE = "Apache 2.0";
    public static final String SWAGGER_LICENSE_URL_MESSAGE = "http://springdoc.org";
    public static final String SWAGGER_TERMS_OF_SERVICE_MESSAGE = "http://swagger.io/terms/";
    /**
     * Mensajes de éxito para Zona
     */
    public static final String ZONA_CREATED_MESSAGE = "Zona creada exitosamente.";
    public static final String ZONA_UPDATED_MESSAGE = "Zona actualizada exitosamente.";
    public static final String ZONA_DELETED_MESSAGE = "Zona eliminada exitosamente.";

    /**
     * Mensajes de excepciones
     */
    public static final String ZONA_NOT_FOUND_MESSAGE = "Esta zona no existe.";
    public static final String NO_DATA_FOUND_MESSAGE = "Datos no encontrados";
    public static final String ZONA_ALREADY_EXISTS = "Ya existe una zona con ese nombre.";
}
