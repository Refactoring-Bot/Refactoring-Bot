package de.BA.refactoringBot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This Class bootstraps Spring and automatically configures it.
 * 
 * @author Stefan Basaric
 *
 */
@SpringBootApplication
public class AlternativeRefactoringBotApplication {

	/**
	 * This method starts the Spring-Application.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		SpringApplication.run(AlternativeRefactoringBotApplication.class, args);
	}
}
