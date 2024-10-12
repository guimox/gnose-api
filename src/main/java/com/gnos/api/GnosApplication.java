package com.gnos.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableAutoConfiguration
@RestController
@RequestMapping("/api")
public class GnosApplication {

	@GetMapping
	public String applicationStatus() {
		return "Application is up and running, everything okay";
	}

	public static void main(String[] args) {
		SpringApplication.run(GnosApplication.class, args);
	}

}
