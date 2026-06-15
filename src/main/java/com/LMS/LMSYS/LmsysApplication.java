package com.LMS.LMSYS;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling

public class LmsysApplication {

	public static void main(String[] args) {
		SpringApplication.run(LmsysApplication.class, args);
	}

}
