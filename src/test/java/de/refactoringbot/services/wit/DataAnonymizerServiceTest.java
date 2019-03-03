package de.refactoringbot.services.wit;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataAnonymizerServiceTest {

	DataAnonymizerService anonymizer = new DataAnonymizerService();
	
	private static final Logger logger = LoggerFactory.getLogger(DataAnonymizerServiceTest.class);
	
	public static final String emailComment = "Can you rename this method to 'newName'? Contact me for more information fake@fakemail.com.";
	public static final String urlComment = "Can you reorder these modifiers? Please do it like https://github.com/Refactoring-Bot/Refactoring-Bot.";
	public static final String userComment = "@Botname could you remove the parameter c from this method?";
	public static final String repoNameComment = "Refactoring-Bot/Refactoring-Bot is able to override methods? Can you do it too?";
	public static final String tokenComment = "My fake github token is 32t247dff2ia218a8946d65c6c240d25f544g14n.";
	public static final String allInOneComment = "Could you rename this method to newName like the project LHommeDeBat/Refactoring-Bot from https://github.com/LHommeDeBat/Refactoring-Bot, @Botname? If you have issues contact me at fake.man@fakemail.de. You can use my github token 32t247dff2ia218a8946d65c6c240d25f544g14n.";
	
	@Test
	public void testEmailComment() {
		SoftAssertions softAssertions = new SoftAssertions();
		String anonymizedComment = anonymizer.anonymizeComment(emailComment);
		logger.debug(anonymizedComment);
		softAssertions.assertThat(anonymizedComment).isEqualTo("Can you rename this method to 'newName'? Contact me for more information EMAIL.");
	}
	
	@Test
	public void testUrlComment() {
		SoftAssertions softAssertions = new SoftAssertions();
		String anonymizedComment = anonymizer.anonymizeComment(urlComment);
		logger.debug(anonymizedComment);
		softAssertions.assertThat(anonymizedComment).isEqualTo("Can you reorder these modifiers? Please do it like URL.");
	}
	
	@Test
	public void testUserComment() {
		SoftAssertions softAssertions = new SoftAssertions();
		String anonymizedComment = anonymizer.anonymizeComment(userComment);
		logger.debug(anonymizedComment);
		softAssertions.assertThat(anonymizedComment).isEqualTo("@USER could you remove the parameter c from this method?");
	}
	
	@Test
	public void testRepoNameComment() {
		SoftAssertions softAssertions = new SoftAssertions();
		String anonymizedComment = anonymizer.anonymizeComment(repoNameComment);
		logger.debug(anonymizedComment);
		softAssertions.assertThat(anonymizedComment).isEqualTo("REPO is able to override methods? Can you do it too?");
	}
	
	@Test
	public void testTokenComment() {
		SoftAssertions softAssertions = new SoftAssertions();
		String anonymizedComment = anonymizer.anonymizeComment(tokenComment);
		logger.debug(anonymizedComment);
		softAssertions.assertThat(anonymizedComment).isEqualTo("My fake github token is TOKEN.");
	}
	
	@Test
	public void testAllInOneComment() {
		SoftAssertions softAssertions = new SoftAssertions();
		String anonymizedComment = anonymizer.anonymizeComment(allInOneComment);
		logger.debug(anonymizedComment);
		softAssertions.assertThat(anonymizedComment).isEqualTo("Could you rename this method to newName like the project REPO from URL, @USER? If you have issues contact me at EMAIL. You can use my github token TOKEN.");
	}
}
