package com.playko.messaging.service.services;

import com.playko.messaging.service.dto.EmailLog;
import com.playko.messaging.service.dto.SendNotification;
import com.playko.messaging.service.exception.MessageNotSendException;
import com.playko.messaging.service.repository.EmailLogRepository;
import com.playko.messaging.service.service.impl.NotificationService;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTests {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private EmailLogRepository emailLogRepository;

    @InjectMocks
    private NotificationService notificationService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        // Crear un MimeMessage real pero controlado
        mimeMessage = new MimeMessage((Session) null);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    private SendNotification buildNotification() {
        return SendNotification.builder()
                .to("destinatario@test.com")
                .subject("Asunto de prueba")
                .body("Cuerpo del mensaje")
                .animalName("Firulais")
                .commentAuthorName("Juan")
                .commentDate(LocalDateTime.now())
                .animalId(123L)
                .commentId(456L)
                .commentAuthorEmail("juan@test.com")
                .build();
    }

    @Test
    void sendNotification_shouldSendAndSaveLog_whenMessageIdPresent() throws Exception {
        // Arrange
        mimeMessage.setHeader("Message-ID", "<123@domain.com>");
        when(templateEngine.process(eq("email"), any(Context.class))).thenReturn("<html>Email</html>");

        // Act
        notificationService.sendNotification(buildNotification());

        // Assert
        verify(javaMailSender).send(mimeMessage);
        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(logCaptor.capture());

        EmailLog savedLog = logCaptor.getValue();
        assertThat(savedLog.getMessageId()).isEqualTo("<123@domain.com>");
        assertThat(savedLog.getTo()).isEqualTo("destinatario@test.com");
        assertThat(savedLog.getSubject()).isEqualTo("Asunto de prueba");
    }

    @Test
    void sendNotification_shouldSendAndSaveLog_whenMessageIdIsNull() {
        // Arrange
        when(templateEngine.process(eq("email"), any(Context.class))).thenReturn("<html>Email</html>");

        // Act
        notificationService.sendNotification(buildNotification());

        // Assert
        verify(javaMailSender).send(mimeMessage);
        ArgumentCaptor<EmailLog> logCaptor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getMessageId()).isNull();
    }

    @Test
    void sendNotification_shouldThrowException_whenJavaMailSenderFails() {
        // Arrange
        when(templateEngine.process(eq("email"), any(Context.class))).thenReturn("<html>Email</html>");
        doThrow(new RuntimeException("Error de envío")).when(javaMailSender).send(any(MimeMessage.class));

        // Act & Assert
        assertThrows(MessageNotSendException.class, () ->
                notificationService.sendNotification(buildNotification())
        );
        verify(emailLogRepository, never()).save(any());
    }

    @Test
    void sendNotification_shouldThrowException_whenTemplateEngineFails() {
        // Arrange
        when(templateEngine.process(eq("email"), any(Context.class)))
                .thenThrow(new RuntimeException("Error en template"));

        // Act & Assert
        assertThrows(MessageNotSendException.class, () ->
                notificationService.sendNotification(buildNotification())
        );
        verify(emailLogRepository, never()).save(any());
    }

    @Test
    void sendNotification_shouldSaveLogWithNullMessageId_whenHeaderArrayIsEmpty() throws Exception {
        // Arrange
        mimeMessage.setHeader("Message-ID", null); // clear header
        mimeMessage.removeHeader("Message-ID");
        mimeMessage.addHeader("Message-ID", ""); // provoca array con un elemento vacío
        // Esto fuerza que getHeader devuelva un array [""] pero luego se evalúe como vacío
        when(templateEngine.process(eq("email"), any(Context.class))).thenReturn("<html>Email</html>");
        // Simular array vacío de verdad
        mimeMessage = spy(mimeMessage);
        when(mimeMessage.getHeader("Message-ID")).thenReturn(new String[0]);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Act
        notificationService.sendNotification(buildNotification());

        // Assert
        ArgumentCaptor<EmailLog> captor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(captor.capture());
        assertThat(captor.getValue().getMessageId()).isNull();
    }

    @Test
    void sendNotification_shouldTakeFirstMessageId_whenMultiplePresent() throws MessagingException {
        // Arrange
        String[] ids = { "<first@domain.com>", "<second@domain.com>" };
        MimeMessage spyMessage = spy(mimeMessage);
        when(spyMessage.getHeader("Message-ID")).thenReturn(ids);
        when(javaMailSender.createMimeMessage()).thenReturn(spyMessage);
        when(templateEngine.process(eq("email"), any(Context.class))).thenReturn("<html>Email</html>");

        // Act
        notificationService.sendNotification(buildNotification());

        // Assert
        ArgumentCaptor<EmailLog> captor = ArgumentCaptor.forClass(EmailLog.class);
        verify(emailLogRepository).save(captor.capture());
        assertThat(captor.getValue().getMessageId()).isEqualTo("<first@domain.com>");
    }

    @Test
    void sendNotification_shouldThrowException_whenSetToFails() throws Exception {
        // Arrange
        MimeMessage spyMessage = spy(new MimeMessage((Session) null));
        when(javaMailSender.createMimeMessage()).thenReturn(spyMessage);
        // Forzar fallo en setTo lanzando MessagingException
        doThrow(new jakarta.mail.MessagingException("Fallo en setTo"))
                .when(spyMessage).setRecipients(any(), (Address[]) any());

        // Act & Assert
        assertThrows(MessageNotSendException.class, () ->
                notificationService.sendNotification(buildNotification())
        );
        verify(templateEngine, never()).process(anyString(), any());
        verify(emailLogRepository, never()).save(any());
    }
}