package com.cosmin.fitness_tracker_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class FitnessTrackerApiApplication {

	 static void main(String[] args) {
		SpringApplication.run(FitnessTrackerApiApplication.class, args);
	}

}
