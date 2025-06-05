package com.playko.zoologico.constants;

public class ExceptionMessages {
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

    private ExceptionMessages() {
        throw new IllegalStateException("Utility class");
    }
}