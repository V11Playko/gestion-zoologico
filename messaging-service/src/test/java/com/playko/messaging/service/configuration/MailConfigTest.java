package com.playko.messaging.service.configuration;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Properties;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MailConfig.class)
@TestPropertySource("classpath:email.properties")
class MailConfigTest {

    @Autowired
    private JavaMailSender javaMailSender;

    @Test
    void javaMailSenderBeanIsCreated() {
        assertNotNull(javaMailSender);
        assertTrue(javaMailSender instanceof JavaMailSenderImpl);
    }

    @Test
    void javaMailSenderHasCorrectConfiguration() {
        JavaMailSenderImpl sender = (JavaMailSenderImpl) javaMailSender;

        assertEquals("smtp.gmail.com", sender.getHost());
        assertEquals(587, sender.getPort());
        assertNotNull(sender.getUsername());
        assertNotNull(sender.getPassword());

        Properties props = sender.getJavaMailProperties();
        assertEquals("smtp", props.getProperty("mail.transport.protocol"));
        assertEquals("true", props.getProperty("mail.smtp.auth"));
        assertEquals("true", props.getProperty("mail.smtp.starttls.enable"));
        assertEquals("smtp.gmail.com", props.getProperty("mail.smtp.ssl.trust"));
        assertEquals("false", props.getProperty("mail.debug"));
    }
}