package com.example.proyecto_cobros;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication
public class ProyectoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoApplication.class, args);
	}

	//  Método para encriptar con SHA-256
	@Bean
	public MessageDigest passwordEncoder() throws NoSuchAlgorithmException {
		return MessageDigest.getInstance("SHA-256");
	}
}