package de.refactoringbot.refactorings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import org.assertj.core.api.SoftAssertions;
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
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.supportedrefactorings.RenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataClassRenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataClassRenameMethod.TestDataInnerClassRenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataClassWithCallOfTargetMethod;
import de.refactoringbot.resources.renamemethod.TestDataSiblingClassRenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataSubClassRenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataSuperClassRenameMethod;

public class RenameMethodTest extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(RenameMethodTest.class);

	private TestDataClassRenameMethod renameMethodTestClass = new TestDataClassRenameMethod();
	private TestDataInnerClassRenameMethod renameMethodInnerTestClass = renameMethodTestClass.new TestDataInnerClassRenameMethod();
	private TestDataClassWithCallOfTargetMethod renameMethodCallerTestClass = new TestDataClassWithCallOfTargetMethod();
	private TestDataSuperClassRenameMethod renameMethodSuperClass = new TestDataSuperClassRenameMethod();
	private TestDataSubClassRenameMethod renameMethodSubClass = new TestDataSubClassRenameMethod();
	private TestDataSiblingClassRenameMethod renameMethodSiblingClass = new TestDataSiblingClassRenameMethod();

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testRenameMethod() throws Exception {
		testRenameMethod(renameMethodTestClass.getLineOfMethodToBeRenamed(true), "changedMethodName");
	}

	@Test
	public void testRenameMethodUnchangedMethodName() throws Exception {
		exception.expect(BotRefactoringException.class);
		testRenameMethod(renameMethodTestClass.getLineOfMethodToBeRenamed(true), "getLineOfMethodToBeRenamed");
	}

	/**
	 * Tries to rename the method in the given line to the given new method name.
	 * Checks if refactoring was successful in all test data classes
	 * 
	 * @param lineNumberOfMethodToBeRenamed
	 * @param newMethodName
	 * @throws Exception
	 */
	private void testRenameMethod(int lineNumberOfMethodToBeRenamed, String newMethodName) throws Exception {
		// arrange
		File fileWithCodeSmell = createTempCopyOfTestResourcesFile(TestDataClassRenameMethod.class);
		File fileOfSuperClass = createTempCopyOfTestResourcesFile(TestDataSuperClassRenameMethod.class);
		File fileOfSubClass = createTempCopyOfTestResourcesFile(TestDataSubClassRenameMethod.class);
		File fileWithCallerMethod = createTempCopyOfTestResourcesFile(TestDataClassWithCallOfTargetMethod.class);
		File fileOfSiblingClass = createTempCopyOfTestResourcesFile(TestDataSiblingClassRenameMethod.class);

		CompilationUnit cuOriginalFileWithCodeSmell = JavaParser.parse(fileWithCodeSmell);
		CompilationUnit cuOriginalFileWithCallerMethod = JavaParser.parse(fileWithCallerMethod);
		CompilationUnit cuOriginalFileOfSuperClass = JavaParser.parse(fileOfSuperClass);
		CompilationUnit cuOriginalFileOfSubClass = JavaParser.parse(fileOfSubClass);
		CompilationUnit cuOriginalFileOfSiblingClass = JavaParser.parse(fileOfSiblingClass);

		MethodDeclaration originalMethod = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfMethodToBeRenamed, cuOriginalFileWithCodeSmell);
		MethodDeclaration originalDummyMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodTestClass.getLineOfMethodToBeRenamed(), cuOriginalFileWithCodeSmell);
		MethodDeclaration originalCallerMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodTestClass.getLineOfMethodThatCallsMethodToBeRenamed(), cuOriginalFileWithCodeSmell);
		MethodDeclaration originalCallerMethodInnerClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodInnerTestClass.getLineNumberOfCallerInInnerClass(), cuOriginalFileWithCodeSmell);
		MethodDeclaration originalCallerMethodInDifferentFile = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodCallerTestClass.getLineOfCallerMethodInDifferentFile(), cuOriginalFileWithCallerMethod);
		MethodDeclaration originalMethodInSuperClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodSuperClass.getLineOfMethodToBeRenamed(true), cuOriginalFileOfSuperClass);
		MethodDeclaration originalMethodInSubClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodSubClass.getLineOfMethodToBeRenamed(true), cuOriginalFileOfSubClass);
		MethodDeclaration originalMethodInSiblingClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodSiblingClass.getLineOfMethodToBeRenamed(true), cuOriginalFileOfSiblingClass);
		MethodDeclaration originalCallerMethodInSiblingClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodSiblingClass.getLineNumberOfCallerInSiblingClass(), cuOriginalFileOfSiblingClass);
		MethodDeclaration originalMethodWithTargetMethodSignatureInInnerClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(renameMethodInnerTestClass.getLineOfMethodToBeRenamed(true),
						cuOriginalFileWithCodeSmell);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(originalMethod).isNotNull();
		softAssertions.assertThat(originalDummyMethod).isNotNull();
		softAssertions.assertThat(originalCallerMethod).isNotNull();
		softAssertions.assertThat(originalCallerMethodInnerClass).isNotNull();
		softAssertions.assertThat(originalCallerMethodInDifferentFile).isNotNull();
		softAssertions.assertThat(originalMethodInSuperClass).isNotNull();
		softAssertions.assertThat(originalMethodInSubClass).isNotNull();
		softAssertions.assertThat(originalMethodInSiblingClass).isNotNull();
		softAssertions.assertThat(originalCallerMethodInSiblingClass).isNotNull();
		softAssertions.assertThat(originalMethodWithTargetMethodSignatureInInnerClass).isNotNull();
		softAssertions.assertAll();

		GitConfiguration gitConfig = new GitConfiguration();
		gitConfig.setRepoFolder(getAbsolutePathOfTempFolder());

		ArrayList<String> javaRoots = new ArrayList<>();
		javaRoots.add(getAbsolutePathOfTestsFolder());
		BotIssue issue = new BotIssue();
		issue.setFilePath(fileWithCodeSmell.getName());
		issue.setLine(lineNumberOfMethodToBeRenamed);
		issue.setJavaRoots(javaRoots);
		issue.setRefactorString(newMethodName);
		issue.setAllJavaFiles(Arrays.asList(fileWithCodeSmell.getCanonicalPath(), fileOfSuperClass.getCanonicalPath(),
				fileOfSubClass.getCanonicalPath(), fileWithCallerMethod.getCanonicalPath(),
				fileOfSiblingClass.getCanonicalPath()));

		// act
		RenameMethod refactoring = new RenameMethod();
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);

		// assert
		CompilationUnit cuRefactoredFileWithCodeSmell = JavaParser.parse(fileWithCodeSmell);
		CompilationUnit cuRefactoredFileWithCallerMethod = JavaParser.parse(fileWithCallerMethod);
		CompilationUnit cuRefactoredFileOfSuperClass = JavaParser.parse(fileOfSuperClass);
		CompilationUnit cuRefactoredFileOfSubClass = JavaParser.parse(fileOfSubClass);
		CompilationUnit cuRefactoredFileOfSiblingClass = JavaParser.parse(fileOfSiblingClass);

		// assert that target method has been renamed
		MethodDeclaration renamedMethodDeclaration = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfMethodToBeRenamed, cuRefactoredFileWithCodeSmell);
		assertThat(renamedMethodDeclaration).isNotNull();
		assertThat(renamedMethodDeclaration.getNameAsString()).isEqualTo(newMethodName);

		// assert that dummy method with same name is unchanged
		int lineNumberOfDummyMethod = renameMethodTestClass.getLineOfMethodToBeRenamed();
		MethodDeclaration dummyMethod = RefactoringHelper.getMethodDeclarationByLineNumber(lineNumberOfDummyMethod,
				cuRefactoredFileWithCodeSmell);
		assertThat(dummyMethod).isNotNull();
		assertThat(dummyMethod.getNameAsString()).isEqualTo(originalDummyMethod.getNameAsString());

		// assert that inner class method with same name as target method is unchanged
		int lineNumberOfMethodWithTargetMethodSignatureInInnerClass = renameMethodInnerTestClass
				.getLineOfMethodToBeRenamed(true);
		MethodDeclaration methodWithTargetMethodSignatureInInnerClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfMethodWithTargetMethodSignatureInInnerClass,
						cuRefactoredFileWithCodeSmell);
		assertThat(methodWithTargetMethodSignatureInInnerClass).isNotNull();
		assertThat(methodWithTargetMethodSignatureInInnerClass.getNameAsString())
				.isEqualTo(originalMethodWithTargetMethodSignatureInInnerClass.getNameAsString());

		// assert that caller method in same file has been refactored
		int lineNumberOfCallerMethod = renameMethodTestClass.getLineOfMethodThatCallsMethodToBeRenamed();
		MethodDeclaration methodWithTargetMethodCalls = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfCallerMethod, cuRefactoredFileWithCodeSmell);
		assertThat(methodWithTargetMethodCalls).isNotNull();
		assertThat(countNumberOfMethodCalls(methodWithTargetMethodCalls, newMethodName)).isEqualTo(1);

		// assert that caller method in different file has been refactored
		int lineNumberOfCallerInDifferentFile = renameMethodCallerTestClass.getLineOfCallerMethodInDifferentFile();
		MethodDeclaration methodInDifferentFileWithTargetMethodCalls = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfCallerInDifferentFile, cuRefactoredFileWithCallerMethod);
		assertThat(methodInDifferentFileWithTargetMethodCalls).isNotNull();
		assertThat(countNumberOfMethodCalls(methodInDifferentFileWithTargetMethodCalls, newMethodName)).isEqualTo(1);

		// assert that target's super class has been refactored
		int lineNumberOfMethodInSuperClass = renameMethodSuperClass.getLineOfMethodToBeRenamed(true);
		MethodDeclaration methodInSuperClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfMethodInSuperClass, cuRefactoredFileOfSuperClass);
		assertThat(methodInSuperClass).isNotNull();
		assertThat(methodInSuperClass.getNameAsString()).isEqualTo(newMethodName);

		// assert that target's sub class has been refactored
		int lineNumberOfMethodInSubClass = renameMethodSubClass.getLineOfMethodToBeRenamed(true);
		MethodDeclaration methodInSubClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfMethodInSubClass, cuRefactoredFileOfSubClass);
		assertThat(methodInSubClass).isNotNull();
		assertThat(methodInSubClass.getNameAsString()).isEqualTo(newMethodName);

		// assert that target's sibling has been refactored
		int lineNumberOfMethodInSiblingClass = renameMethodSiblingClass.getLineOfMethodToBeRenamed(true);
		MethodDeclaration methodInSiblingClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfMethodInSiblingClass, cuRefactoredFileOfSiblingClass);
		assertThat(methodInSiblingClass).isNotNull();
		assertThat(methodInSiblingClass.getNameAsString()).isEqualTo(newMethodName);

		// assert that caller method in target's sibling class has been refactored
		int lineNumberOfCallerMethodInSiblingClass = renameMethodSiblingClass.getLineNumberOfCallerInSiblingClass();
		MethodDeclaration methodInSiblingClassWithSiblingMethodCall = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfCallerMethodInSiblingClass,
						cuRefactoredFileOfSiblingClass);
		assertThat(methodInSiblingClassWithSiblingMethodCall).isNotNull();
		assertThat(countNumberOfMethodCalls(methodInSiblingClassWithSiblingMethodCall, newMethodName)).isEqualTo(1);
	}

	/**
	 * @param methodWithMethodCalls
	 * @param calledMethodName
	 * @return number of method calls inside the given method to a method with the
	 *         given name
	 */
	private int countNumberOfMethodCalls(MethodDeclaration methodWithMethodCalls, String calledMethodName) {
		int numberOfMethodsWithNewMethodName = 0;
		for (MethodCallExpr methodCall : methodWithMethodCalls.getBody().get().findAll(MethodCallExpr.class)) {
			if (methodCall.getNameAsString().equals(calledMethodName)) {
				numberOfMethodsWithNewMethodName++;
			}
		}
		return numberOfMethodsWithNewMethodName;
	}

}
