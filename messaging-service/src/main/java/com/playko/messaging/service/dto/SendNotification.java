package com.playko.messaging.service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class SendNotification {

   @NotBlank
    private String to;

    private String subject;

    private String body;

    private Long animalId;

    private String animalName;

    private Long commentId;

    private LocalDateTime commentDate;
    private String commentAuthorName;
    private String commentAuthorEmail;

 // 🔹 campos para adjunto
 private byte[] attachment;
 private String attachmentName;
}