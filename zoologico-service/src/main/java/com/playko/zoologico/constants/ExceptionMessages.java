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
    public static final String NON_NEGATIVE_PAGE_NUMBER_MESSAGE = "El numero de pagina no puede ser negativo.";
    public static final String ID_ZONA_INVALID_MESSAGE = "El ID de la zona es inválido.";
    public static final String ERROR_GETTING_MAIL_TOKEN_MESSAGE = "Error obteniendo el correo del token";
    public static final String ERROR_GENERATING_EXCEL_MESSAGE = "Error generando el excel.";
    public static final String NO_COMENTARIOS_EN_FECHA_MESSAGE = "No se encontraron comentarios para la fecha: ";
    public static final String FECHA_INVALIDA = "Formato de fecha inválido. El formato correcto es 'yyyy-MM-dd'.";


    private ExceptionMessages() {
        throw new IllegalStateException("Utility class");
    }
}