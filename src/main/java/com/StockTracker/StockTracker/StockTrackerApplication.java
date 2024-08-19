package com.StockTracker.StockTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@SpringBootApplication
public class StockTrackerApplication {


	public static void main(String[] args) {
		// Main application logic (for example, starting your Spring Boot application)
		SpringApplication.run(StockTrackerApplication.class, args);

	}



	@GetMapping("/root")
	public String apiRoot(){
		return "Hello Trader!!";
	}
}
