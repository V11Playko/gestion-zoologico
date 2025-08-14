package com.playko.messaging.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "email_logs")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmailLog {
    @Id
    private String id;

    private String messageId;            // Message-ID devuelto por SMTP
    private String to;                   // destinatario
    private String subject;
    private String body;                 // texto o html (según lo que mandes)
    private String commentAuthorName;
    private String commentAuthorEmail;
    private String animalName;
    private Long animalId;
    private Long commentId;
    private LocalDateTime sentAt;
}
