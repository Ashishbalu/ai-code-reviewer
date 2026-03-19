package com.ashish.aiCodeReviewer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
public class AiCodeReviewerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiCodeReviewerApplication.class, args);
	}

}

