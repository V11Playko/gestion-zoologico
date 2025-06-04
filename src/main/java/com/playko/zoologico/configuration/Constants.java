package com.playko.zoologico.configuration;

public class Constants {
    public static final String RESPONSE_MESSAGE_KEY = "Mensaje";

    public static final String USUARIO_CREATED_MESSAGE = "Usuario empleado creado correctamente.";

    public static final String COMENTARIO_AGREGADO_MESSAGE = "Comentario agregado correctamente.";

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
     * Mensajes de éxito para Especie
     */
    public static final String ESPECIE_CREATED_MESSAGE = "Especie creada correctamente.";

    public static final String ESPECIE_UPDATED_MESSAGE = "Especie actualizada correctamente.";

    public static final String ESPECIE_DELETED_MESSAGE = "Especie eliminada correctamente.";

    /**
     * Mensajes de éxito para Animal
     */
    public static final String ANIMAL_CREATED_MESSAGE = "Animal creado correctamente.";

    public static final String ANIMAL_UPDATED_MESSAGE = "Animal actualizado correctamente.";

    public static final String ANIMAL_DELETED_MESSAGE = "Animal eliminado correctamente.";

    /**
     * Mensajes de excepciones
     */
    public static final String ZONA_NOT_FOUND_MESSAGE = "Esta zona no existe.";

    public static final String NO_DATA_FOUND_MESSAGE = "Datos no encontrados";

    public static final String ZONA_ALREADY_EXISTS = "Ya existe una zona con ese nombre.";

    public static final String ESPECIE_ALREADY_EXISTS_MESSAGE = "Ya existe una especie con ese nombre.";

    public static final String ESPECIE_CON_ANIMALES_MESSAGE = "No se puede eliminar la especie porque tiene animales asociados.";

    public static final String ESPECIE_NOT_FOUND_MESSAGE = "Esta especie no existe.";

    public static final String ANIMAL_NOT_FOUND_MESSAGE = "Este animal no existe.";

    public static final String EMAIL_ALREADY_EXISTS_MESSAGE = "Ya existe este correo.";

    public static final String ROLE_NOT_FOUND_MESSAGE = "Este role no existe.";

    public static final String COMENTARIO_PADRE_NOT_FOUND_MESSAGE = "Comentario padre no encontrado.";

    public static final String ZONA_CON_ANIMALES_MESSAGE = "Esta zona tiene animales, no se puede eliminar.";
    public static final String USER_NOT_FOUND_MESSAGE = "Usuario no encontrado";
    public static final String ZONA_ESPECIE_MISMATCH_MESSAGE = "La zona asignada al animal no coincide con la zona de su especie.";
    public static final String COMENTARIO_ANIMAL_MISMATCH_MESSAGE = "El comentario padre pertenece a un animal diferente.";
    public static final String ANIMAL_SIN_COMENTARIOS_MESSAGE = "Este animal aún no tiene comentarios.";
    private Constants() {
        throw new IllegalStateException("Utility class");
    }

}
