package de.refactoringbot.configuration;

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
	private String witClientToken = "";
	private boolean enableScheduling;

	public String getBotRefactoringDirectory() {
		return botRefactoringDirectory;
	}

	public void setBotRefactoringDirectory(String botRefactoringDirectory) {
		this.botRefactoringDirectory = botRefactoringDirectory;
	}

	public String getWitClientToken() {
		return witClientToken;
	}

	public void setWitClientToken(String witClientToken) {
		this.witClientToken = witClientToken;
	}

	public boolean isEnableScheduling() {
		return enableScheduling;
	}

	public void setEnableScheduling(boolean enableScheduling) {
		this.enableScheduling = enableScheduling;
	}

}
