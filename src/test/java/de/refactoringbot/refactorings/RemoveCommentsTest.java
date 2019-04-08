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
	private TestDataClassRemoveComments missingOverrideTestClass = new TestDataClassRemoveComments();

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testRemoveLineComments() throws Exception {
		// This should also remove the line directly below, but not the next one
		testRemoveComment(6, "// Normal comment - This one shouldn't be removed");
	}

	@Test
	public void testRemoveBlockComment() throws Exception {
		testRemoveComment(11, "return a + b;");
	}

	private void testRemoveComment(int line, String expectedResult) throws Exception {
		// arrange
		File tempFile = createTempCopyOfTestResourcesFile(TestDataClassRemoveComments.class);
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
