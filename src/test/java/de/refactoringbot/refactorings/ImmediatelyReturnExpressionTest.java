package de.refactoringbot.refactorings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.supportedrefactorings.ImmediatelyReturnExpression;
import de.refactoringbot.resources.immediatelyreturn.TestDataClassImmediatelyReturnExpression;
import de.refactoringbot.testutils.TestUtils;

public class ImmediatelyReturnExpressionTest extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(ImmediatelyReturnExpressionTest.class);

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testImmediatelyReturnResult() throws Exception {
		// arrange
		int lineOfResultAssignment = TestDataClassImmediatelyReturnExpression.getLineOfResultAssignment();

		// act
		File modifiedTempFile = performRefactoring(lineOfResultAssignment);

		// assert
		String lineContent = getStrippedContentFromFile(modifiedTempFile, lineOfResultAssignment);
		assertThat(lineContent).isEqualTo("return 12 + 0;");
	}

	@Test
	public void testImmediatelyReturnResultEmptyLines() throws Exception {
		// arrange
		int lineOfResultAssignment = TestDataClassImmediatelyReturnExpression
				.getLineOfResultAssignmentWithCommentLineAfter();

		// act
		File modifiedTempFile = performRefactoring(lineOfResultAssignment);

		// assert
		String lineContent = getStrippedContentFromFile(modifiedTempFile, lineOfResultAssignment + 1);
		assertThat(lineContent).isEqualTo("return 17 + 0;");
	}

	@Test
	public void testImmediatelyReturnResultWithMultipleIndependentReturns() throws Exception {
		// arrange
		int lineOfResultAssignment = TestDataClassImmediatelyReturnExpression.getLineOfSecondResultAssignment();

		// act
		File modifiedTempFile = performRefactoring(lineOfResultAssignment);

		// assert
		String lineContent = getStrippedContentFromFile(modifiedTempFile, lineOfResultAssignment);
		assertThat(lineContent).isEqualTo("return 28 + 0;");
	}

	@Test
	public void testReturnVariableUsedMoreThanOnce() throws Exception {
		exception.expect(BotRefactoringException.class);

		// arrange
		int lineOfResultAssignment = TestDataClassImmediatelyReturnExpression.getLineOfAssignmentUsedMoreThanOnce();

		// act
		performRefactoring(lineOfResultAssignment);
	}

	@Test
	public void testReturnOfComplexAssignment() throws Exception {
		// arrange
		int lineOfResultAssignment = TestDataClassImmediatelyReturnExpression.getLineOfComplexResultAssignment();

		// act
		File modifiedTempFile = performRefactoring(lineOfResultAssignment);

		// assert
		String lineContent = getStrippedContentFromFile(modifiedTempFile, lineOfResultAssignment);
		assertThat(lineContent).isEqualTo("return 42 + (dummyMethod() * a);");
	}

	@Test
	public void testResultAssignmentSpanningMultipleLines() throws Exception {
		// arrange
		int lineOfResultAssignment = TestDataClassImmediatelyReturnExpression
				.getLineOfResultAssignmentSpanningMultipleLines();

		// act
		File modifiedTempFile = performRefactoring(lineOfResultAssignment);

		// assert
		String lineContent = getStrippedContentFromFile(modifiedTempFile, lineOfResultAssignment);
		assertThat(lineContent).isEqualTo(
				"return 47 + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod();");
	}

	@Test
	public void testResultAssignmentWithLineComment() throws Exception {
		// arrange
		int lineOfResultAssignment = TestDataClassImmediatelyReturnExpression
				.getLineOfResultAssignmentWithLineComment();

		// act
		File modifiedTempFile = performRefactoring(lineOfResultAssignment);

		// assert
		String lineContentComment = getStrippedContentFromFile(modifiedTempFile, lineOfResultAssignment);
		String lineContentReturn = getStrippedContentFromFile(modifiedTempFile, lineOfResultAssignment + 1);
		assertThat(lineContentComment).isEqualTo("// comment");
		assertThat(lineContentReturn).isEqualTo("return 53;");
	}

	@Test
	public void testMultipleVariableDeclarationsAtSameLine() throws Exception {
		exception.expect(UnsupportedOperationException.class);
		
		// arrange
		int lineOfResultAssignment = TestDataClassImmediatelyReturnExpression.getLineOfMultipleVariableDeclarations();
		
		// act
		performRefactoring(lineOfResultAssignment);
	}

	private File performRefactoring(int line) throws Exception {
		File tempFile = createTempCopyOfTestResourcesFile(TestDataClassImmediatelyReturnExpression.class);
		ImmediatelyReturnExpression refactoring = new ImmediatelyReturnExpression();

		ArrayList<String> javaRoots = new ArrayList<>();
		javaRoots.add(TestUtils.getAbsolutePathOfTestsFolder());
		BotIssue issue = new BotIssue();
		issue.setFilePath(tempFile.getName());
		issue.setLine(line);
		issue.setJavaRoots(javaRoots);
		List<String> allJavaFiles = new ArrayList<>();
		allJavaFiles.add(tempFile.getCanonicalPath());
		issue.setAllJavaFiles(allJavaFiles);
		GitConfiguration gitConfig = new GitConfiguration();
		gitConfig.setRepoFolder(getAbsolutePathOfTempFolder());

		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);

		return tempFile;
	}
}
