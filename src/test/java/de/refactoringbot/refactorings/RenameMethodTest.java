package de.refactoringbot.refactorings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.supportedrefactorings.RenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataClassRenameMethod;

public class RenameMethodTest extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(RenameMethodTest.class);
	private TestDataClassRenameMethod renameMethodTestClass = new TestDataClassRenameMethod();

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testRenameMethod() throws Exception {
		// arrange
		File tempFile = createTempCopyOfTestResourcesFile(TestDataClassRenameMethod.class);
		BotIssue issue = new BotIssue();
		GitConfiguration gitConfig = new GitConfiguration();
		RenameMethod refactoring = new RenameMethod();
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed(true);
		int lineNumberOfSecondMethodNotToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed();
		CompilationUnit cuOriginalFile = JavaParser.parse(tempFile);
		String originalMethodName = RefactoringHelper
				.getMethodByLineNumberOfMethodName(lineNumberOfMethodToBeRenamed, cuOriginalFile).getNameAsString();
		String newMethodName = "changedMethodName";
		String originalSecondMethodName = RefactoringHelper
				.getMethodByLineNumberOfMethodName(lineNumberOfSecondMethodNotToBeRenamed, cuOriginalFile)
				.getNameAsString();
		assertThat(originalSecondMethodName).isEqualTo(originalMethodName);

		gitConfig.setRepoFolder("");
		issue.setFilePath(tempFile.getAbsolutePath());
		issue.setLine(lineNumberOfMethodToBeRenamed);
		issue.setJavaRoots(new ArrayList<>());
		issue.setRefactorString(newMethodName);
		issue.setAllJavaFiles(Arrays.asList(tempFile.getAbsolutePath()));

		// act
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);

		// assert
		CompilationUnit cu = JavaParser.parse(tempFile);

		// assert that method has been renamed
		MethodDeclaration renamedMethodDeclaration = RefactoringHelper
				.getMethodByLineNumberOfMethodName(lineNumberOfMethodToBeRenamed, cu);
		assertThat(renamedMethodDeclaration).isNotNull();
		assertThat(renamedMethodDeclaration.getNameAsString()).isEqualTo(newMethodName);

		// assert that second method with same name has not been renamed
		int lineNumberOfSecondMethodWithSameNameAsOriginalMethod = renameMethodTestClass.getLineOfMethodToBeRenamed();
		MethodDeclaration secondMethodDeclaration = RefactoringHelper
				.getMethodByLineNumberOfMethodName(lineNumberOfSecondMethodWithSameNameAsOriginalMethod, cu);
		assertThat(secondMethodDeclaration).isNotNull();
		assertThat(secondMethodDeclaration.getNameAsString()).isEqualTo(originalSecondMethodName);

		// assert that caller method has been refactored as well
		int lineNumberOfCallerMethod = renameMethodTestClass.getLineOfMethodThatCallsMethodToBeRenamed();
		MethodDeclaration callerMethod = RefactoringHelper.getMethodByLineNumberOfMethodName(lineNumberOfCallerMethod,
				cu);
		assertThat(callerMethod).isNotNull();
		int numberOfMethodsWithNewMethodName = 0;
		for (MethodCallExpr methodCall : callerMethod.getBody().get().findAll(MethodCallExpr.class)) {
			if (methodCall.getNameAsString().equals(newMethodName)) {
				numberOfMethodsWithNewMethodName++;
			}
		}
		assertThat(numberOfMethodsWithNewMethodName).isEqualTo(1);
	}
}
