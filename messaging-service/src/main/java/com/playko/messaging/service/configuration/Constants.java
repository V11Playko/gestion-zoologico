package com.playko.messaging.service.configuration;

public class Constants {
    public static final String RESPONSE_MESSAGE_KEY = "Mensaje";
    public static final String MESSAGE_NOT_SEND = "El mensaje no se envio correctamente.";
    public static final String NOTIFICATIONS_SEND_ENDPOINT = "/api/notifications/send";


    private Constants() {
        throw new IllegalStateException("Utility class");
    }
}
