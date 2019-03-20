package de.refactoringbot.refactorings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
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

	private File fileOfTestClass;
	private File fileOfSuperClass;
	private File fileOfSubClass;
	private File fileWithCallerMethod;
	private File fileOfSiblingClass;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void createTempCopiesOfTestResourceFiles() throws IOException {
		fileOfTestClass = createTempCopyOfTestResourcesFile(TestDataClassRenameMethod.class);
		fileOfSuperClass = createTempCopyOfTestResourcesFile(TestDataSuperClassRenameMethod.class);
		fileOfSubClass = createTempCopyOfTestResourcesFile(TestDataSubClassRenameMethod.class);
		fileWithCallerMethod = createTempCopyOfTestResourcesFile(TestDataClassWithCallOfTargetMethod.class);
		fileOfSiblingClass = createTempCopyOfTestResourcesFile(TestDataSiblingClassRenameMethod.class);
	}

	@Test
	public void testTargetClassRefactored() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed(true);
		String newMethodName = "newMethodName";

		CompilationUnit cuOriginalFileOfTestClass = JavaParser.parse(fileOfTestClass);
		MethodDeclaration originalDummyMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodTestClass.getLineOfMethodToBeRenamed(), cuOriginalFileOfTestClass);
		MethodDeclaration originalMethodWithTargetMethodSignatureInInnerClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(renameMethodInnerTestClass.getLineOfMethodToBeRenamed(true),
						cuOriginalFileOfTestClass);

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert
		CompilationUnit cuRefactoredFileOfTestClass = JavaParser.parse(fileOfTestClass);

		// assert that target method has been renamed
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfTestClass, lineNumberOfMethodToBeRenamed,
				newMethodName);

		// assert that dummy method with same name is unchanged
		int lineNumberOfDummyMethod = renameMethodTestClass.getLineOfMethodToBeRenamed();
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfTestClass, lineNumberOfDummyMethod,
				originalDummyMethod.getNameAsString());

		// assert that inner class method with same name as target method is unchanged
		int lineNumberOfMethodWithTargetMethodSignatureInInnerClass = renameMethodInnerTestClass
				.getLineOfMethodToBeRenamed(true);
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfTestClass,
				lineNumberOfMethodWithTargetMethodSignatureInInnerClass,
				originalMethodWithTargetMethodSignatureInInnerClass.getNameAsString());

		// assert that caller method in same file has been refactored
		int lineNumberOfCallerMethod = renameMethodTestClass.getLineOfMethodThatCallsMethodToBeRenamed();
		assertThatNumberOfMethodCallsIsEqualToExpected(cuRefactoredFileOfTestClass, lineNumberOfCallerMethod,
				newMethodName, 1);
	}

	@Test
	public void testCallerClassRefactored() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		filesToConsider.add(fileWithCallerMethod);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed(true);
		String newMethodName = "newMethodName";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert that caller method in different file has been refactored
		CompilationUnit cuRefactoredFileWithCallerMethod = JavaParser.parse(fileWithCallerMethod);
		int lineNumberOfCallerInDifferentFile = renameMethodCallerTestClass.getLineOfCallerMethodInDifferentFile();
		assertThatNumberOfMethodCallsIsEqualToExpected(cuRefactoredFileWithCallerMethod,
				lineNumberOfCallerInDifferentFile, newMethodName, 1);
	}

	@Test
	public void testSuperClassRefactored() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		filesToConsider.add(fileOfSuperClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed(true);
		String newMethodName = "newMethodName";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert that target's super class has been refactored
		CompilationUnit cuRefactoredFileOfSuperClass = JavaParser.parse(fileOfSuperClass);
		int lineNumberOfMethodInSuperClass = renameMethodSuperClass.getLineOfMethodToBeRenamed(true);
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfSuperClass, lineNumberOfMethodInSuperClass,
				newMethodName);
	}

	@Test
	public void testSubClassRefactored() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		filesToConsider.add(fileOfSubClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed(true);
		String newMethodName = "newMethodName";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert that target's sub class has been refactored
		CompilationUnit cuRefactoredFileOfSubClass = JavaParser.parse(fileOfSubClass);
		int lineNumberOfMethodInSubClass = renameMethodSubClass.getLineOfMethodToBeRenamed(true);
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfSubClass, lineNumberOfMethodInSubClass, newMethodName);
	}

	@Test
	public void testSiblingClassRefactored() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		filesToConsider.add(fileOfSiblingClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed(true);
		String newMethodName = "newMethodName";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert
		CompilationUnit cuRefactoredFileOfSiblingClass = JavaParser.parse(fileOfSiblingClass);

		// assert that target's sibling has been refactored
		int lineNumberOfMethodInSiblingClass = renameMethodSiblingClass.getLineOfMethodToBeRenamed(true);
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfSiblingClass, lineNumberOfMethodInSiblingClass,
				newMethodName);

		// assert that caller method in target's sibling class has been refactored
		int lineNumberOfCallerMethodInSiblingClass = renameMethodSiblingClass.getLineNumberOfCallerInSiblingClass();
		assertThatNumberOfMethodCallsIsEqualToExpected(cuRefactoredFileOfSiblingClass,
				lineNumberOfCallerMethodInSiblingClass, newMethodName, 1);
	}

	@Test
	public void testRenameMethodUnchangedMethodName() throws Exception {
		exception.expect(BotRefactoringException.class);

		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed(true);
		String newMethodName = "getLineOfMethodToBeRenamed";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);
	}

	/**
	 * Tries to rename the method in the given file and line to the given new method name.
	 * 
	 * @param filesToConsider All files that make up the repository for the specific test
	 * @param targetFile
	 * @param lineNumberOfMethodToBeRenamed
	 * @param newMethodName
	 * @throws Exception
	 */
	private void performRenameMethod(List<File> filesToConsider, File targetFile, int lineNumberOfMethodToBeRenamed,
			String newMethodName) throws Exception {
		GitConfiguration gitConfig = new GitConfiguration();
		gitConfig.setRepoFolder(getAbsolutePathOfTempFolder());

		BotIssue issue = new BotIssue();
		ArrayList<String> javaRoots = new ArrayList<>();
		javaRoots.add(getAbsolutePathOfTestsFolder());
		issue.setFilePath(targetFile.getName());
		issue.setLine(lineNumberOfMethodToBeRenamed);
		issue.setJavaRoots(javaRoots);
		issue.setRefactorString(newMethodName);
		List<String> allJavaFiles = new ArrayList<>();
		for (File f : filesToConsider) {
			allJavaFiles.add(f.getCanonicalPath());
		}
		issue.setAllJavaFiles(allJavaFiles);

		RenameMethod refactoring = new RenameMethod();
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);
	}
	
	/**
	 * @param cu
	 * @param lineNumberOfMethodUnderTest
	 * @param expectedMethodName
	 */
	private void assertThatMethodNameIsEqualToExpected(CompilationUnit cu, int lineNumberOfMethodUnderTest,
			String expectedMethodName) {
		MethodDeclaration methodUnderTest = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfMethodUnderTest, cu);
		assertThat(methodUnderTest).isNotNull();
		assertThat(methodUnderTest.getNameAsString()).isEqualTo(expectedMethodName);
	}

	/**
	 * @param cu
	 * @param lineNumberOfMethodUnderTest
	 * @param calledMethodName
	 * @param expectedNumberOfMethodCalls
	 */
	private void assertThatNumberOfMethodCallsIsEqualToExpected(CompilationUnit cu, int lineNumberOfMethodUnderTest,
			String calledMethodName, int expectedNumberOfMethodCalls) {
		MethodDeclaration methodInSiblingClassWithSiblingMethodCall = RefactoringHelper
				.getMethodDeclarationByLineNumber(lineNumberOfMethodUnderTest, cu);
		assertThat(methodInSiblingClassWithSiblingMethodCall).isNotNull();
		assertThat(countNumberOfMethodCalls(methodInSiblingClassWithSiblingMethodCall, calledMethodName))
				.isEqualTo(expectedNumberOfMethodCalls);
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
