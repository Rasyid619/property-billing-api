package com.propertybilling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
/**
 * Application entry point for the Property Billing API.
 */
public class PropertyBillingApiApplication {

	/**
	 * Starts the Spring Boot application.
	 *
	 * @param args command-line arguments supplied at startup
	 */
	public static void main(String[] args) {
		SpringApplication.run(PropertyBillingApiApplication.class, args);
	}
}
