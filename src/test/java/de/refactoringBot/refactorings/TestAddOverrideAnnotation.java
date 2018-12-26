package de.refactoringBot.refactorings;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.supportedRefactorings.AddOverrideAnnotation;
import de.refactoringBot.resources.TestDataClassMissingOverrideAnnotation;

public class TestAddOverrideAnnotation extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(TestAddOverrideAnnotation.class);
	private TestDataClassMissingOverrideAnnotation missingOverrideTestClass = new TestDataClassMissingOverrideAnnotation();
	
	
	@Test
	public void testAddOverrideAnnotation() throws Exception {
		testAddOverrideAnnotation(missingOverrideTestClass.getLineOfMethodWithMissingOverrideAnnotation());
	}
	
	@Test
	public void testAddOverrideAnnotationWithExistingAnnotation() throws Exception {
		testAddOverrideAnnotation(missingOverrideTestClass.getLineOfMethodWithMissingOverrideAnnotation2());
	}
	
	@Test
	public void testAddOverrideAnnotationWithJavadoc() throws Exception {
		testAddOverrideAnnotation(missingOverrideTestClass.getLineOfMethodWithMissingOverrideAnnotation3());
	}
	
	@Test
	public void testAddOverrideAnnotationWithExistingAnnotationAndJavadoc() throws Exception {
		testAddOverrideAnnotation(missingOverrideTestClass.getLineOfMethodWithMissingOverrideAnnotation4());
	}

	@Test
	public void testNoAnnotationRequired() throws Exception {
		// TODO assert that no second override annotation was added
	}
	
	private void testAddOverrideAnnotation(int lineNumberOfMethodWithMissingOverride) throws Exception {
		// arrange
		File tempFile = getTempCopyOfResourcesFile("TestDataClassMissingOverrideAnnotation.java");
		BotIssue issue = new BotIssue();
		GitConfiguration gitConfig = new GitConfiguration();
		AddOverrideAnnotation refactoring = new AddOverrideAnnotation();
		
		gitConfig.setRepoFolder("");
		issue.setFilePath(tempFile.getAbsolutePath());
		issue.setLine(lineNumberOfMethodWithMissingOverride);

		// act
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);
		
		// assert
		String lineContent = getStrippedContentFromFile(tempFile, lineNumberOfMethodWithMissingOverride);
		assertEquals("@Override", lineContent);
	}

}
