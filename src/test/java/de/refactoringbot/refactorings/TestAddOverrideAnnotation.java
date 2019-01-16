package de.refactoringbot.refactorings;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.supportedrefactorings.AddOverrideAnnotation;
import de.refactoringbot.resources.addoverrideannotation.TestDataClassMissingOverrideAnnotation;

public class TestAddOverrideAnnotation extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(TestAddOverrideAnnotation.class);
	private TestDataClassMissingOverrideAnnotation missingOverrideTestClass = new TestDataClassMissingOverrideAnnotation();

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Override
	public Class<TestDataClassMissingOverrideAnnotation> getTestResourcesClass() {
		return TestDataClassMissingOverrideAnnotation.class;
	}
	
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
	public void testAddOverrideAnnotationWithExistingOverrideAnnotation() throws Exception {
		exception.expect(BotRefactoringException.class);
		testAddOverrideAnnotation(missingOverrideTestClass.getLineOfMethodWithoutMissingOverrideAnnotation());
	}

	private void testAddOverrideAnnotation(int lineNumberOfMethodWithMissingOverride) throws Exception {
		// arrange
		File tempFile = getTempCopyOfTestResourcesFile();
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
