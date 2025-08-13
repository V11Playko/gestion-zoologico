package com.playko.zoologico;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ZoologicoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZoologicoApplication.class, args);
	}

}
