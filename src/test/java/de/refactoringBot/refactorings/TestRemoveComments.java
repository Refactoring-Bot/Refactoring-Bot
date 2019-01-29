package de.refactoringBot.refactorings;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.exceptions.BotRefactoringException;
import de.refactoringBot.refactoring.supportedRefactorings.RemoveCommentedOutCode;
import de.refactoringBot.resources.TestDataClassMissingOverrideAnnotation;
import java.time.LocalDate;

public class TestRemoveComments extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(TestRemoveComments.class);
	private TestDataClassMissingOverrideAnnotation missingOverrideTestClass = new TestDataClassMissingOverrideAnnotation();

	@Rule
	public final ExpectedException exception = ExpectedException.none();

        @Test
	public void testRemoveLineComment() throws Exception {
            removeCommentByLine(6, "int c = a + b;");
	}
        
        @Test
	public void testRemoveBlockComment() throws Exception {
            removeCommentByLine(8, "c = b - a;");
	}
        
        @Test
	public void testRemoveJavadocComment() throws Exception {
            removeCommentByLine(12, "return a + b;");
	}
        
	private void removeCommentByLine(int line, String expectedResult) throws Exception {
		// arrange
		File tempFile = getTempCopyOfResourcesFile("TestDataClassCommentedOutCode.java");
		BotIssue issue = new BotIssue();
		GitConfiguration gitConfig = new GitConfiguration();
		RemoveCommentedOutCode refactoring = new RemoveCommentedOutCode();

		gitConfig.setRepoFolder("");
		issue.setFilePath(tempFile.getAbsolutePath());
		issue.setLine(line);
                issue.setCreationDate("2018-01-29");

		// act
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);

		// assert
		String lineContent = getStrippedContentFromFile(tempFile, line);
		assertEquals(expectedResult, lineContent);
	}        

}
