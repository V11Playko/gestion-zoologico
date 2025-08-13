package com.playko.messaging.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SendNotification {

    private String to;

    private String subject;

    private String body;

    private Long animalId;

    private String animalName;

    private Long commentId;

    private LocalDateTime commentDate;
    private String commentAuthorName;
    private String commentAuthorEmail;
}