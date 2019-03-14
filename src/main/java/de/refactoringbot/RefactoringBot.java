package de.refactoringbot;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This Class bootstraps Spring and automatically configures it.
 * 
 * @author Stefan Basaric
 *
 */
@SpringBootApplication
@EnableScheduling
public class RefactoringBot {

	/**
	 * This method starts the Spring-Application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(RefactoringBot.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		return new ModelMapper();
	}
}
