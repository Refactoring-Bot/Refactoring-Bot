package de.refactoringbot.services.wit;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class DataAnonymizerServiceTest {

	DataAnonymizerService anonymizer = new DataAnonymizerService();
	
	public static final String emailComment = "Can you rename this method to 'newName'? Contact me for more information fake@fakemail.com.";
	public static final String urlComment = "Can you reorder these modifiers? Please do it like https://github.com/Refactoring-Bot/Refactoring-Bot.";
	public static final String userComment = "@Botname could you remove the parameter c from this method?";
	public static final String repoNameComment = "Refactoring-Bot/Refactoring-Bot is able to override methods? Can you do it too?";
	public static final String tokenComment = "My fake github token is 32t247dff2ia218a8946d65c6c240d25f544g14n.";
	public static final String allInOneComment = "Could you rename this method to newName like the project LHommeDeBat/Refactoring-Bot from https://github.com/LHommeDeBat/Refactoring-Bot, @Botname? If you have issues contact me at fake.man@fakemail.de. You can use my github token 32t247dff2ia218a8946d65c6c240d25f544g14n.";
	
	@Test
	public void testEmailComment() {
		String anonymizedComment = anonymizer.anonymizeComment(emailComment);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(anonymizedComment).isEqualTo("Can you rename this method to 'newName'? Contact me for more information EMAIL.");
		softAssertions.assertAll();
	}
	
	@Test
	public void testUrlComment() {
		String anonymizedComment = anonymizer.anonymizeComment(urlComment);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(anonymizedComment).isEqualTo("Can you reorder these modifiers? Please do it like URL.");
		softAssertions.assertAll();
	}
	
	@Test
	public void testUserComment() {
		String anonymizedComment = anonymizer.anonymizeComment(userComment);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(anonymizedComment).isEqualTo("@USER could you remove the parameter c from this method?");
		softAssertions.assertAll();
	}
	
	@Test
	public void testRepoNameComment() {
		SoftAssertions softAssertions = new SoftAssertions();
		String anonymizedComment = anonymizer.anonymizeComment(repoNameComment);
		softAssertions.assertThat(anonymizedComment).isEqualTo("REPO is able to override methods? Can you do it too?");
		softAssertions.assertAll();
	}
	
	@Test
	public void testTokenComment() {
		String anonymizedComment = anonymizer.anonymizeComment(tokenComment);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(anonymizedComment).isEqualTo("My fake github token is TOKEN.");
		softAssertions.assertAll();
	}
	
	@Test
	public void testAllInOneComment() {
		String anonymizedComment = anonymizer.anonymizeComment(allInOneComment);
		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(anonymizedComment).isEqualTo("Could you rename this method to newName like the project REPO from URL, @USER? If you have issues contact me at EMAIL. You can use my github token TOKEN.");
		softAssertions.assertAll();
	}
}
