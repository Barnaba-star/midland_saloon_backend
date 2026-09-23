package com.midland.saloon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SaloonApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaloonApplication.class, args);
	}

}
