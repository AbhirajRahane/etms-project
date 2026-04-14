package com.company.etms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EtmsApplication {

	/**
	 * Entry point of the ETMS Spring Boot application.
	 * 
	 * This method starts the entire application, sets up the Spring context,
	 * and launches the embedded server.
	 *
	 * @param args Command-line arguments passed to the application
	 * @author Abhiraj Rahane
	 */
	public static void main(String[] args) {
		SpringApplication.run(EtmsApplication.class, args);
	}

}