package com.payflow.payflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main application class for the Payflow application.
 * This class enables Spring Boot's scheduling capabilities for automatic payslip generation.
 */
@SpringBootApplication
@EnableScheduling
public class PayflowApplication {

	public static void main(String[] args) {
		SpringApplication.run(PayflowApplication.class, args);
	}

}
