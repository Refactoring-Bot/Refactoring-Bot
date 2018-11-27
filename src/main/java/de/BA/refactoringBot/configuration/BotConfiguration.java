package de.BA.refactoringBot.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * This class is used within the application.yml file.
 * 
 * @author Stefan Basaric
 *
 */
@Component
@ConfigurationProperties(prefix = "bot")
public class BotConfiguration {

	private String botRefactoringDirectory = "";

	public String getBotRefactoringDirectory() {
		return botRefactoringDirectory;
	}

	public void setBotRefactoringDirectory(String botRefactoringDirectory) {
		this.botRefactoringDirectory = botRefactoringDirectory;
	}

}
