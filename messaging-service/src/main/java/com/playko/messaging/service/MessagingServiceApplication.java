package com.playko.messaging.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class MessagingServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(MessagingServiceApplication.class, args);
	}
}