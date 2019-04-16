package de.refactoringbot.refactorings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.supportedrefactorings.RenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataClassImplementingEmptyInterface;
import de.refactoringbot.resources.renamemethod.TestDataClassImplementingEmptyInterface.TestDataInnerClassImplementingEmptyInterface;
import de.refactoringbot.resources.renamemethod.TestDataClassRenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataClassRenameMethod.TestDataInnerClassRenameMethod;
import de.refactoringbot.resources.renamemethod.TestDataClassRenameMethod.TestDataInnerClassWithInterfaceImpl;
import de.refactoringbot.resources.renamemethod.TestDataClassWithCallOfTargetMethod;
import de.refactoringbot.resources.renamemethod.TestDataEmptyInterface;
import de.refactoringbot.resources.renamemethod.TestDataInterfaceRenameMethod;
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
	private TestDataInnerClassWithInterfaceImpl renameMethodInnerClassWithInterfaceImpl = renameMethodTestClass.new TestDataInnerClassWithInterfaceImpl();
	private TestDataClassImplementingEmptyInterface renameMethodTestClassWithEmptyInterfaceImpl = new TestDataClassImplementingEmptyInterface();
	private TestDataInnerClassImplementingEmptyInterface renameMethodInnerClassWithEmptyInterfaceImpl = renameMethodTestClassWithEmptyInterfaceImpl.new TestDataInnerClassImplementingEmptyInterface();

	private File fileOfTestClass;
	private File fileOfSuperClass;
	private File fileOfSubClass;
	private File fileWithCallerMethod;
	private File fileOfSiblingClass;
	private File fileOfInterface;
	private File fileOfEmptyInterface;
	private File fileOfTestClassImplementingEmptyInterface;

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Before
	public void createTempCopiesOfTestResourceFiles() throws IOException {
		fileOfTestClass = createTempCopyOfTestResourcesFile(TestDataClassRenameMethod.class);
		fileOfSuperClass = createTempCopyOfTestResourcesFile(TestDataSuperClassRenameMethod.class);
		fileOfSubClass = createTempCopyOfTestResourcesFile(TestDataSubClassRenameMethod.class);
		fileWithCallerMethod = createTempCopyOfTestResourcesFile(TestDataClassWithCallOfTargetMethod.class);
		fileOfSiblingClass = createTempCopyOfTestResourcesFile(TestDataSiblingClassRenameMethod.class);
		fileOfInterface = createTempCopyOfTestResourcesFile(TestDataInterfaceRenameMethod.class);
		fileOfEmptyInterface = createTempCopyOfTestResourcesFile(TestDataEmptyInterface.class);
		fileOfTestClassImplementingEmptyInterface = createTempCopyOfTestResourcesFile(
				TestDataClassImplementingEmptyInterface.class);
	}

	/**
	 * Test whether the refactoring was performed correctly in the target class
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTargetClassRefactored() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed(true);
		String newMethodName = "newMethodName";

		CompilationUnit cuOriginalFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		MethodDeclaration originalDummyMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodTestClass.getLineOfMethodToBeRenamed(), cuOriginalFileOfTestClass);
		MethodDeclaration originalMethodWithTargetMethodSignatureInInnerClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(renameMethodInnerTestClass.getLineOfMethodToBeRenamed(true),
						cuOriginalFileOfTestClass);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(originalDummyMethod).isNotNull();
		softAssertions.assertThat(originalMethodWithTargetMethodSignatureInInnerClass).isNotNull();
		softAssertions.assertAll();

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert
		CompilationUnit cuRefactoredFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);

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

	/**
	 * Test whether the refactoring was performed correctly in a different class
	 * which contains a method calling the refactored target method
	 * 
	 * @throws Exception
	 */
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
		CompilationUnit cuRefactoredFileWithCallerMethod = StaticJavaParser.parse(fileWithCallerMethod);
		int lineNumberOfCallerInDifferentFile = renameMethodCallerTestClass.getLineOfCallerMethodInDifferentFile();
		assertThatNumberOfMethodCallsIsEqualToExpected(cuRefactoredFileWithCallerMethod,
				lineNumberOfCallerInDifferentFile, newMethodName, 1);
	}

	/**
	 * Test whether the refactoring was performed correctly in the super class of
	 * the target class (ancestor)
	 * 
	 * @throws Exception
	 */
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
		CompilationUnit cuRefactoredFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		int lineNumberOfMethodInSuperClass = renameMethodSuperClass.getLineOfMethodToBeRenamed(true);
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfSuperClass, lineNumberOfMethodInSuperClass,
				newMethodName);
	}

	/**
	 * Test whether the refactoring was performed correctly in the sub class of the
	 * target class (descendant)
	 * 
	 * @throws Exception
	 */
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
		CompilationUnit cuRefactoredFileOfSubClass = StaticJavaParser.parse(fileOfSubClass);
		int lineNumberOfMethodInSubClass = renameMethodSubClass.getLineOfMethodToBeRenamed(true);
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfSubClass, lineNumberOfMethodInSubClass, newMethodName);
	}

	/**
	 * Test whether the refactoring was performed correctly in the sibling class of
	 * the target class
	 * 
	 * @throws Exception
	 */
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
		CompilationUnit cuRefactoredFileOfSiblingClass = StaticJavaParser.parse(fileOfSiblingClass);

		// assert that target's sibling has been refactored
		int lineNumberOfMethodInSiblingClass = renameMethodSiblingClass.getLineOfMethodToBeRenamed(true);
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfSiblingClass, lineNumberOfMethodInSiblingClass,
				newMethodName);

		// assert that caller method in target's sibling class has been refactored
		int lineNumberOfCallerMethodInSiblingClass = renameMethodSiblingClass.getLineNumberOfCallerInSiblingClass();
		assertThatNumberOfMethodCallsIsEqualToExpected(cuRefactoredFileOfSiblingClass,
				lineNumberOfCallerMethodInSiblingClass, newMethodName, 1);
	}

	/**
	 * Test whether the refactoring was performed correctly in an interface
	 * implemented by the target class
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInterfaceRefactored() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		filesToConsider.add(fileOfInterface);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfInterfaceMethod();
		String newMethodName = "newMethodName";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert that method in interface has been refactored
		CompilationUnit cuRefactoredFileOfInterface = StaticJavaParser.parse(fileOfInterface);
		List<MethodDeclaration> methodDeclarations = cuRefactoredFileOfInterface.findAll(MethodDeclaration.class);
		assertThat(methodDeclarations).size().isEqualTo(1);
		assertThat(methodDeclarations.get(0).getNameAsString()).isEqualTo(newMethodName);
	}

	/**
	 * The target class extends a super class and implements an interface. Test that
	 * the super class was correctly refactored
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInterfaceMethodInSuperClassRefactored() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		filesToConsider.add(fileOfInterface);
		filesToConsider.add(fileOfSuperClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfInterfaceMethod();
		String newMethodName = "newMethodName";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert that method in super class has been refactored
		CompilationUnit cuRefactoredFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		int lineNumberOfMethodInSuperClass = renameMethodSuperClass.getLineOfInterfaceMethod();
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfSuperClass, lineNumberOfMethodInSuperClass,
				newMethodName);
	}

	/**
	 * Test that a refactoring of an inner class (implementing the same interface as
	 * the outer class) results in a correct refactoring of both, the inner and the
	 * outer class. Also test whether the implemented interface and super class of
	 * the outer class was successfully refactored
	 * 
	 * @throws Exception
	 */
	@Test
	public void testInnerClassWithInterfaceRefactoring() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		filesToConsider.add(fileOfInterface);
		filesToConsider.add(fileOfSuperClass);
		int lineNumberOfMethodToBeRenamed = renameMethodInnerClassWithInterfaceImpl.getLineOfInterfaceMethod();
		String newMethodName = "newMethodName";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert
		CompilationUnit cuRefactoredFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);

		// assert that method in inner class has been refactored
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfTestClass, lineNumberOfMethodToBeRenamed,
				newMethodName);

		// assert that method in outer class has been refactored
		int lineNumberOfMethodInOuterClass = renameMethodTestClass.getLineOfInterfaceMethod();
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfTestClass, lineNumberOfMethodInOuterClass,
				newMethodName);

		// assert that method in interface has been refactored
		CompilationUnit cuRefactoredFileOfInterface = StaticJavaParser.parse(fileOfInterface);
		List<MethodDeclaration> methodDeclarations = cuRefactoredFileOfInterface.findAll(MethodDeclaration.class);
		assertThat(methodDeclarations).size().isEqualTo(1);
		assertThat(methodDeclarations.get(0).getNameAsString()).isEqualTo(newMethodName);

		// assert that super class of outer class has been refactored
		CompilationUnit cuRefactoredFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		int lineNumberOfMethodInSuperClass = renameMethodSuperClass.getLineOfInterfaceMethod();
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfSuperClass, lineNumberOfMethodInSuperClass,
				newMethodName);
	}

	/**
	 * Two classes sharing the same interface should only lead to a refactoring in
	 * both classes, if the common interface declares the target method signature.
	 * This is not given in this test case
	 * 
	 * @throws Exception
	 */
	@Test
	public void testTwoClassesWithSameMethodSigAndEmptyInterface() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClassImplementingEmptyInterface);
		filesToConsider.add(fileOfEmptyInterface);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClassWithEmptyInterfaceImpl.getLineOfMethodToBeRenamed();
		String newMethodName = "newMethodName";

		CompilationUnit cuOriginalFileOfTestClassImplementingEmptyInterface = StaticJavaParser
				.parse(fileOfTestClassImplementingEmptyInterface);
		MethodDeclaration originalMethodInInnerClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				renameMethodTestClassWithEmptyInterfaceImpl.getLineOfMethodToBeRenamed(),
				cuOriginalFileOfTestClassImplementingEmptyInterface);

		assertThat(originalMethodInInnerClass).isNotNull();

		// act
		performRenameMethod(filesToConsider, fileOfTestClassImplementingEmptyInterface, lineNumberOfMethodToBeRenamed,
				newMethodName);

		// assert
		CompilationUnit cuRefactoredFileOfTestClassImplementingEmptyInterface = StaticJavaParser
				.parse(fileOfTestClassImplementingEmptyInterface);

		// assert that method in outer class has been refactored
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfTestClassImplementingEmptyInterface,
				lineNumberOfMethodToBeRenamed, newMethodName);

		// assert that inner class method remained unchanged
		int lineNumberOfInnerClassMethod = renameMethodInnerClassWithEmptyInterfaceImpl.getLineOfMethodToBeRenamed();
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfTestClassImplementingEmptyInterface,
				lineNumberOfInnerClassMethod, originalMethodInInnerClass.getNameAsString());
	}

	/**
	 * Test that the refactoring algorithm finds the correct method in case that
	 * there is an inner class before the target method declaration
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRenamingOfMethodPlacedAfterInnerClass() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodPlacedInAndAfterInnerClass();
		String newMethodName = "newMethodName";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);

		// assert that method in outer class (the method for which the actual renaming
		// was intended) has been refactored
		CompilationUnit cuRefactoredFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		assertThatMethodNameIsEqualToExpected(cuRefactoredFileOfTestClass, lineNumberOfMethodToBeRenamed,
				newMethodName);
	}

	/**
	 * @throws Exception
	 *             expected in this case because method name is unchanged
	 */
	@Test
	public void testUnchangedMethodName() throws Exception {
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
	 * @throws Exception
	 *             expected in this case because there is already a signature that
	 *             would result from the refactoring
	 */
	@Test
	public void testSignatureAlreadyExists() throws Exception {
		exception.expect(BotRefactoringException.class);

		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		int lineNumberOfMethodToBeRenamed = renameMethodTestClass.getLineOfMethodToBeRenamed();
		String newMethodName = "getLineOfMethodThatCallsMethodToBeRenamed";

		// act
		performRenameMethod(filesToConsider, fileOfTestClass, lineNumberOfMethodToBeRenamed, newMethodName);
	}

	/**
	 * Tries to rename the method in the given file and line to the given new method
	 * name.
	 * 
	 * @param filesToConsider
	 *            All files that make up the repository for the specific test
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
