package com.midland.saloon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.TimeZone;

@SpringBootApplication
@EnableScheduling
public class SaloonApplication {

	public static void main(String[] args) {
		// Servers run on UTC, three hours behind the salons: every time stamped
		// with LocalDateTime.now() (sales, shifts, payroll "generated") read
		// 14:58 for 17:58, and the day turned over at 03:00. Tanzania time,
		// wherever the server is.
		TimeZone.setDefault(TimeZone.getTimeZone("Africa/Dar_es_Salaam"));
		SpringApplication.run(SaloonApplication.class, args);
	}

}
