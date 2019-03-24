package de.refactoringbot.refactorings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveCommentedOutCode;
import de.refactoringbot.resources.removecomments.TestDataClassRemoveComments;

public class RemoveCommentsTest extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(RemoveCommentsTest.class);

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Override
	public Class<TestDataClassRemoveComments> getTestResourcesClass() {
		return TestDataClassRemoveComments.class;
	}
        
        @Test
	public void testRemoveLineComments() throws Exception {
            // This should also remove the line below (since it is also commented out code)
		testRemoveComment(6,"int c = a + b;");
	}
	
        @Test
	public void testRemoveLineCommentOnlyCommentedCode() throws Exception {
            // This should only remove the SonarQube line, since the ones below don't contain code
		testRemoveComment(19,"// Normal comment here");
	}
        
	@Test
	public void testRemoveBlockComment() throws Exception {
		testRemoveComment(10,"/**");
	}
        
        @Test
	public void testRemoveJavadocComment() throws Exception {
		testRemoveComment(14,"}");
	}

	private void testRemoveComment(int line, String expectedResult) throws Exception {
		// arrange
		File tempFile = getTempCopyOfTestResourcesFile();
		BotIssue issue = new BotIssue();
		GitConfiguration gitConfig = new GitConfiguration();
		RemoveCommentedOutCode refactoring = new RemoveCommentedOutCode();

		gitConfig.setRepoFolder("");
		issue.setFilePath(tempFile.getAbsolutePath());
		issue.setLine(line);

		// act
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);

		// assert
		String lineContent = getStrippedContentFromFile(tempFile, line);
		assertThat(lineContent).isEqualTo(expectedResult);
	}

}
