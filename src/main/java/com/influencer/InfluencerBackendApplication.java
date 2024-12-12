package com.influencer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
@EnableScheduling
public class InfluencerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(InfluencerBackendApplication.class, args);
	}


}
