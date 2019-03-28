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
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.javadoc.JavadocBlockTag;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveMethodParameter;
import de.refactoringbot.resources.removeparameter.TestDataClassImplementingEmptyInterface;
import de.refactoringbot.resources.removeparameter.TestDataClassImplementingEmptyInterface.TestDataInnerClassImplementingEmptyInterface;
import de.refactoringbot.resources.removeparameter.TestDataClassRemoveParameter;
import de.refactoringbot.resources.removeparameter.TestDataClassRemoveParameter.TestDataInnerClassRemoveParameter;
import de.refactoringbot.resources.removeparameter.TestDataClassRemoveParameter.TestDataInnerClassWithInterfaceImpl;
import de.refactoringbot.resources.removeparameter.TestDataClassWithCallOfTargetMethod;
import de.refactoringbot.resources.removeparameter.TestDataEmptyInterface;
import de.refactoringbot.resources.removeparameter.TestDataInterfaceRemoveParameter;
import de.refactoringbot.resources.removeparameter.TestDataSiblingClassRemoveParameter;
import de.refactoringbot.resources.removeparameter.TestDataSubClassRemoveParameter;
import de.refactoringbot.resources.removeparameter.TestDataSuperClassRemoveParameter;

public class RemoveMethodParameterTest extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(RemoveMethodParameterTest.class);

	private static final String SIBLING_CLASS_NAME = "TestDataSiblingClassRemoveParameter";
	private static final String SUB_CLASS_NAME = "TestDataSubClassRemoveParameter";
	private static final String SUPER_CLASS_NAME = "TestDataSuperClassRemoveParameter";
	private static final String CALL_OF_TARGET_METHOD_CLASS_NAME = "TestDataClassWithCallOfTargetMethod";
	private static final String TARGET_INNER_CLASS_NAME = "TestDataInnerClassRemoveParameter";
	private static final String TARGET_CLASS_NAME = "TestDataClassRemoveParameter";
	private static final String TARGET_INNER_CLASS_WITH_INTERFACE_NAME = "TestDataInnerClassWithInterfaceImpl";
	private static final String CLASS_IMPLEMENTING_EMPTY_INTERFACE = "TestDataClassImplementingEmptyInterface";
	private static final String INNER_CLASS_IMPLEMENTING_EMPTY_INTERFACE = "TestDataInnerClassImplementingEmptyInterface";

	private TestDataClassRemoveParameter removeParameterTestClass = new TestDataClassRemoveParameter();
	private TestDataInnerClassRemoveParameter removeParameterInnerTestClass = removeParameterTestClass.new TestDataInnerClassRemoveParameter();
	private TestDataClassWithCallOfTargetMethod removeParameterCallerTestClass = new TestDataClassWithCallOfTargetMethod();
	private TestDataSuperClassRemoveParameter removeParameterSuperClass = new TestDataSuperClassRemoveParameter();
	private TestDataSubClassRemoveParameter removeParameterSubClass = new TestDataSubClassRemoveParameter();
	private TestDataSiblingClassRemoveParameter removeParameterSiblingClass = new TestDataSiblingClassRemoveParameter();
	private TestDataInnerClassWithInterfaceImpl removeParameterInnerClassWithInterfaceImpl = removeParameterTestClass.new TestDataInnerClassWithInterfaceImpl();
	private TestDataClassImplementingEmptyInterface removeParameterTestClassWithEmptyInterfaceImpl = new TestDataClassImplementingEmptyInterface();
	private TestDataInnerClassImplementingEmptyInterface removeParameterInnerTestClassWithEmptyInterfaceImpl = removeParameterTestClassWithEmptyInterfaceImpl.new TestDataInnerClassImplementingEmptyInterface();

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
		fileOfTestClass = createTempCopyOfTestResourcesFile(TestDataClassRemoveParameter.class);
		fileOfSuperClass = createTempCopyOfTestResourcesFile(TestDataSuperClassRemoveParameter.class);
		fileOfSubClass = createTempCopyOfTestResourcesFile(TestDataSubClassRemoveParameter.class);
		fileWithCallerMethod = createTempCopyOfTestResourcesFile(TestDataClassWithCallOfTargetMethod.class);
		fileOfSiblingClass = createTempCopyOfTestResourcesFile(TestDataSiblingClassRemoveParameter.class);
		fileOfInterface = createTempCopyOfTestResourcesFile(TestDataInterfaceRemoveParameter.class);
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
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		MethodDeclaration originalMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				lineNumberOfMethodWithParameterToBeRemoved, cuOriginalFileOfTestClass);
		MethodDeclaration originalDummyMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterTestClass.getLineNumberOfDummyMethod(0, 0, 0), cuOriginalFileOfTestClass);
		MethodDeclaration originalCallerMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterTestClass.getLineNumberOfCaller(), cuOriginalFileOfTestClass);
		MethodDeclaration originalCallerMethodInnerClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterInnerTestClass.getLineNumberOfCallerInInnerClass(), cuOriginalFileOfTestClass);
		MethodDeclaration originalMethodWithTargetMethodSignatureInInnerClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(
						removeParameterInnerTestClass.getLineOfMethodWithUnusedParameter(0, 0, 0),
						cuOriginalFileOfTestClass);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(originalMethod).isNotNull();
		softAssertions.assertThat(originalDummyMethod).isNotNull();
		softAssertions.assertThat(originalCallerMethod).isNotNull();
		softAssertions.assertThat(originalCallerMethodInnerClass).isNotNull();
		softAssertions.assertThat(originalMethodWithTargetMethodSignatureInInnerClass).isNotNull();
		softAssertions.assertAll();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert
		CompilationUnit cuRefactoredFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		MethodDeclaration refactoredMethod = getMethodByName(TARGET_CLASS_NAME, originalMethod.getNameAsString(),
				cuRefactoredFileOfTestClass);

		// assert that parameter has been removed from the target method
		assertThat(refactoredMethod).isNotNull();
		assertThat(refactoredMethod.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that parameter has been removed from the Javadoc
		assertParameterNotPresentInJavadoc(refactoredMethod, parameterName);

		// assert that dummy method is unchanged
		MethodDeclaration dummyMethod = getMethodByName(TARGET_CLASS_NAME, originalDummyMethod.getNameAsString(),
				cuRefactoredFileOfTestClass);
		assertThat(dummyMethod).isNotNull();
		assertThat(dummyMethod.getParameterByName(parameterName)).isPresent();

		// assert that inner class method with same name as target method is unchanged
		MethodDeclaration methodWithTargetMethodSignatureInInnerClass = getMethodByName(TARGET_INNER_CLASS_NAME,
				originalMethodWithTargetMethodSignatureInInnerClass.getNameAsString(), cuRefactoredFileOfTestClass);
		assertThat(methodWithTargetMethodSignatureInInnerClass).isNotNull();
		assertThat(methodWithTargetMethodSignatureInInnerClass.getParameterByName(parameterName)).isPresent();

		// assert that caller method in same file has been refactored
		MethodDeclaration methodWithTargetMethodCalls = getMethodByName(TARGET_CLASS_NAME,
				originalCallerMethod.getNameAsString(), cuRefactoredFileOfTestClass);
		assertThat(methodWithTargetMethodCalls).isNotNull();
		assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(methodWithTargetMethodCalls,
				refactoredMethod);

		// assert that caller method in same file in inner class has been refactored
		MethodDeclaration methodWithTargetMethodCallInInnerClass = getMethodByName(TARGET_INNER_CLASS_NAME,
				originalCallerMethodInnerClass.getNameAsString(), cuRefactoredFileOfTestClass);
		assertThat(methodWithTargetMethodCallInInnerClass).isNotNull();
		assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(methodWithTargetMethodCallInInnerClass,
				refactoredMethod);
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
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		CompilationUnit cuOriginalFileWithCallerMethod = StaticJavaParser.parse(fileWithCallerMethod);
		MethodDeclaration originalMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				lineNumberOfMethodWithParameterToBeRemoved, cuOriginalFileOfTestClass);
		MethodDeclaration originalCallerMethodInDifferentFile = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterCallerTestClass.getLineOfCallerMethodInDifferentFile(), cuOriginalFileWithCallerMethod);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(originalMethod).isNotNull();
		softAssertions.assertThat(originalCallerMethodInDifferentFile).isNotNull();
		softAssertions.assertAll();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert
		CompilationUnit cuRefactoredFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		CompilationUnit cuRefactoredFileWithCallerMethod = StaticJavaParser.parse(fileWithCallerMethod);
		MethodDeclaration refactoredMethod = getMethodByName(TARGET_CLASS_NAME, originalMethod.getNameAsString(),
				cuRefactoredFileOfTestClass);

		// assert that caller method in different file has been refactored
		MethodDeclaration methodInDifferentFileWithTargetMethodCalls = getMethodByName(CALL_OF_TARGET_METHOD_CLASS_NAME,
				originalCallerMethodInDifferentFile.getNameAsString(), cuRefactoredFileWithCallerMethod);
		assertThat(methodInDifferentFileWithTargetMethodCalls).isNotNull();
		assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(
				methodInDifferentFileWithTargetMethodCalls, refactoredMethod);
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
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		MethodDeclaration originalMethodInSuperClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSuperClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfSuperClass);
		assertThat(originalMethodInSuperClass).isNotNull();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert that target's super class has been refactored
		CompilationUnit cuRefactoredFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		String methodInSuperClassName = originalMethodInSuperClass.getNameAsString();
		MethodDeclaration methodInSuperClass = getMethodByName(SUPER_CLASS_NAME, methodInSuperClassName,
				cuRefactoredFileOfSuperClass);
		assertThat(methodInSuperClass).isNotNull();
		assertThat(methodInSuperClass.getParameterByName(parameterName).isPresent()).isFalse();
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
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfSubClass = StaticJavaParser.parse(fileOfSubClass);
		MethodDeclaration originalMethodInSubClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSubClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfSubClass);
		assertThat(originalMethodInSubClass).isNotNull();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert that target's sub class has been refactored
		CompilationUnit cuRefactoredFileOfSubClass = StaticJavaParser.parse(fileOfSubClass);
		String methodInSubClassName = originalMethodInSubClass.getNameAsString();
		MethodDeclaration methodInSubClass = getMethodByName(SUB_CLASS_NAME, methodInSubClassName,
				cuRefactoredFileOfSubClass);
		assertThat(methodInSubClass).isNotNull();
		assertThat(methodInSubClass.getParameterByName(parameterName).isPresent()).isFalse();
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
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfSiblingClass = StaticJavaParser.parse(fileOfSiblingClass);
		CompilationUnit cuOriginalFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		MethodDeclaration originalMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				lineNumberOfMethodWithParameterToBeRemoved, cuOriginalFileOfTestClass);
		MethodDeclaration originalMethodInSiblingClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSiblingClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfSiblingClass);
		MethodDeclaration originalCallerMethodInSiblingClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSiblingClass.getLineNumberOfCaller(), cuOriginalFileOfSiblingClass);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(originalMethod).isNotNull();
		softAssertions.assertThat(originalMethodInSiblingClass).isNotNull();
		softAssertions.assertThat(originalCallerMethodInSiblingClass).isNotNull();
		softAssertions.assertAll();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert
		CompilationUnit cuRefactoredFileOfSiblingClass = StaticJavaParser.parse(fileOfSiblingClass);
		CompilationUnit cuRefactoredFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		MethodDeclaration refactoredMethod = getMethodByName(TARGET_CLASS_NAME, originalMethod.getNameAsString(),
				cuRefactoredFileOfTestClass);

		// assert that target's sibling has been refactored
		String methodInSiblingClassName = originalMethodInSiblingClass.getNameAsString();
		MethodDeclaration methodInSiblingClass = getMethodByName(SIBLING_CLASS_NAME, methodInSiblingClassName,
				cuRefactoredFileOfSiblingClass);
		assertThat(methodInSiblingClass).isNotNull();
		assertThat(methodInSiblingClass.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that caller method in target's sibling class has been refactored
		String callerMethodInSiblingClassName = originalCallerMethodInSiblingClass.getNameAsString();
		MethodDeclaration methodInSiblingClassWithSiblingMethodCall = getMethodByName(SIBLING_CLASS_NAME,
				callerMethodInSiblingClassName, cuRefactoredFileOfSiblingClass);
		assertThat(methodInSiblingClassWithSiblingMethodCall).isNotNull();
		assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(methodInSiblingClassWithSiblingMethodCall,
				refactoredMethod);
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
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "b";

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert that method in interface has been refactored
		CompilationUnit cuRefactoredFileOfInterface = StaticJavaParser.parse(fileOfInterface);
		List<MethodDeclaration> methodDeclarations = cuRefactoredFileOfInterface.findAll(MethodDeclaration.class);
		assertThat(methodDeclarations).size().isEqualTo(1);
		assertThat(methodDeclarations.get(0).getParameterByName(parameterName).isPresent()).isFalse();
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
		filesToConsider.add(fileOfSuperClass);
		filesToConsider.add(fileOfInterface);
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		MethodDeclaration originalMethodInSuperClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSuperClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfSuperClass);
		assertThat(originalMethodInSuperClass).isNotNull();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert that method in super class has been refactored
		CompilationUnit cuRefactoredFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		String methodInSuperClassName = originalMethodInSuperClass.getNameAsString();
		MethodDeclaration methodInSuperClass = getMethodByName(SUPER_CLASS_NAME, methodInSuperClassName,
				cuRefactoredFileOfSuperClass);
		assertThat(methodInSuperClass).isNotNull();
		assertThat(methodInSuperClass.getParameterByName(parameterName).isPresent()).isFalse();
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
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterInnerClassWithInterfaceImpl
				.getLineOfMethodWithUnusedParameter(0, 0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		CompilationUnit cuOriginalFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		MethodDeclaration originalInnerClassMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterInnerClassWithInterfaceImpl.getLineOfMethodWithUnusedParameter(0, 0, 0),
				cuOriginalFileOfTestClass);
		MethodDeclaration originalOuterClassMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterTestClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfTestClass);
		MethodDeclaration originalMethodInSuperClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSuperClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfSuperClass);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(originalInnerClassMethod).isNotNull();
		softAssertions.assertThat(originalOuterClassMethod).isNotNull();
		softAssertions.assertThat(originalMethodInSuperClass).isNotNull();
		softAssertions.assertAll();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert
		CompilationUnit cuRefactoredFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);

		// assert that method in inner class has been refactored
		MethodDeclaration refactoredInnerClassMethod = getMethodByName(TARGET_INNER_CLASS_WITH_INTERFACE_NAME,
				originalInnerClassMethod.getNameAsString(), cuRefactoredFileOfTestClass);
		assertThat(refactoredInnerClassMethod).isNotNull();
		assertThat(refactoredInnerClassMethod.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that method in outer class has been refactored
		MethodDeclaration refactoredOuterClassMethod = getMethodByName(TARGET_CLASS_NAME,
				originalOuterClassMethod.getNameAsString(), cuRefactoredFileOfTestClass);
		assertThat(refactoredOuterClassMethod).isNotNull();
		assertThat(refactoredOuterClassMethod.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that method in interface has been refactored
		CompilationUnit cuRefactoredFileOfInterface = StaticJavaParser.parse(fileOfInterface);
		List<MethodDeclaration> methodDeclarations = cuRefactoredFileOfInterface.findAll(MethodDeclaration.class);
		assertThat(methodDeclarations).size().isEqualTo(1);
		assertThat(methodDeclarations.get(0).getParameterByName(parameterName).isPresent()).isFalse();

		// assert that super class of outer class has been refactored
		CompilationUnit cuRefactoredFileOfSuperClass = StaticJavaParser.parse(fileOfSuperClass);
		String methodInSuperClassName = originalMethodInSuperClass.getNameAsString();
		MethodDeclaration methodInSuperClass = getMethodByName(SUPER_CLASS_NAME, methodInSuperClassName,
				cuRefactoredFileOfSuperClass);
		assertThat(methodInSuperClass).isNotNull();
		assertThat(methodInSuperClass.getParameterByName(parameterName).isPresent()).isFalse();
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
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClassWithEmptyInterfaceImpl
				.getLineOfMethodWithUnusedParameter(0, 0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfTestClassImplementingEmptyInterface = StaticJavaParser
				.parse(fileOfTestClassImplementingEmptyInterface);
		MethodDeclaration originalInnerClassMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterInnerTestClassWithEmptyInterfaceImpl.getLineOfMethodWithUnusedParameter(0, 0, 0),
				cuOriginalFileOfTestClassImplementingEmptyInterface);
		MethodDeclaration originalOuterClassMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterTestClassWithEmptyInterfaceImpl.getLineOfMethodWithUnusedParameter(0, 0, 0),
				cuOriginalFileOfTestClassImplementingEmptyInterface);

		SoftAssertions softAssertions = new SoftAssertions();
		softAssertions.assertThat(originalInnerClassMethod).isNotNull();
		softAssertions.assertThat(originalOuterClassMethod).isNotNull();
		softAssertions.assertAll();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClassImplementingEmptyInterface,
				lineNumberOfMethodWithParameterToBeRemoved, parameterName);

		// assert
		CompilationUnit cuRefactoredFileOfTestClassImplementingEmptyInterface = StaticJavaParser
				.parse(fileOfTestClassImplementingEmptyInterface);

		// assert that method in outer class has been refactored
		MethodDeclaration refactoredOuterClassMethod = getMethodByName(CLASS_IMPLEMENTING_EMPTY_INTERFACE,
				originalOuterClassMethod.getNameAsString(), cuRefactoredFileOfTestClassImplementingEmptyInterface);
		assertThat(refactoredOuterClassMethod).isNotNull();
		assertThat(refactoredOuterClassMethod.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that inner class method remained unchanged
		MethodDeclaration refactoredInnerClassMethod = getMethodByName(INNER_CLASS_IMPLEMENTING_EMPTY_INTERFACE,
				originalInnerClassMethod.getNameAsString(), cuRefactoredFileOfTestClassImplementingEmptyInterface);
		assertThat(refactoredInnerClassMethod).isNotNull();
		assertThat(refactoredInnerClassMethod.getParameterByName(parameterName).isPresent()).isTrue();
	}

	/**
	 * Test that the refactoring algorithm finds the correct method in case that
	 * there is an inner class before the target method declaration
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRefactoringOfMethodPlacedAfterInnerClass() throws Exception {
		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass
				.getLineOfMethodPlacedInAndAfterInnerClass(0, 0, 0);
		String parameterName = "b";

		CompilationUnit cuOriginalFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		MethodDeclaration originalMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				lineNumberOfMethodWithParameterToBeRemoved, cuOriginalFileOfTestClass);
		assertThat(originalMethod).isNotNull();

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);

		// assert that method in outer class (the method for which the actual renaming
		// was intended) has been refactored
		CompilationUnit cuRefactoredFileOfTestClass = StaticJavaParser.parse(fileOfTestClass);
		MethodDeclaration refactoredMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				lineNumberOfMethodWithParameterToBeRemoved, cuRefactoredFileOfTestClass);
		assertThat(refactoredMethod).isNotNull();
		assertThat(refactoredMethod.getParameterByName(parameterName).isPresent()).isFalse();
	}

	/**
	 * @throws Exception
	 *             expected in this case because the parameter is used in the method
	 *             body
	 */
	@Test
	public void testRemoveUsedParameter() throws Exception {
		exception.expect(BotRefactoringException.class);

		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "a";

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);
	}

	/**
	 * @throws Exception
	 *             expected in this case because the parameter to be removed does
	 *             not exist in the target method's signature
	 */
	@Test
	public void testRemoveNotExistingParameter() throws Exception {
		exception.expect(BotRefactoringException.class);

		// arrange
		List<File> filesToConsider = new ArrayList<File>();
		filesToConsider.add(fileOfTestClass);
		int lineNumberOfMethodWithParameterToBeRemoved = removeParameterTestClass.getLineOfMethodWithUnusedParameter(0,
				0, 0);
		String parameterName = "d";

		// act
		performRemoveParameter(filesToConsider, fileOfTestClass, lineNumberOfMethodWithParameterToBeRemoved,
				parameterName);
	}

	/**
	 * Tries to remove the parameter from the method in the given line and file.
	 * 
	 * @param filesToConsider
	 *            All files that make up the repository for the specific test
	 * @param targetFile
	 * @param lineNumberOfMethodWithParameterToBeRemoved
	 * @param parameterName
	 * @throws Exception
	 */
	private void performRemoveParameter(List<File> filesToConsider, File targetFile,
			int lineNumberOfMethodWithParameterToBeRemoved, String parameterName) throws Exception {
		GitConfiguration gitConfig = new GitConfiguration();
		gitConfig.setRepoFolder(getAbsolutePathOfTempFolder());

		ArrayList<String> javaRoots = new ArrayList<>();
		javaRoots.add(getAbsolutePathOfTestsFolder());
		BotIssue issue = new BotIssue();
		issue.setFilePath(targetFile.getName());
		issue.setLine(lineNumberOfMethodWithParameterToBeRemoved);
		issue.setJavaRoots(javaRoots);
		issue.setRefactorString(parameterName);
		List<String> allJavaFiles = new ArrayList<>();
		for (File f : filesToConsider) {
			allJavaFiles.add(f.getCanonicalPath());
		}
		issue.setAllJavaFiles(allJavaFiles);

		RemoveMethodParameter refactoring = new RemoveMethodParameter();
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);
	}

	/**
	 * Asserts that the given parameter is not present in the javadoc of the given
	 * method
	 * 
	 * @param methodDeclaration
	 * @param parameterName
	 */
	private void assertParameterNotPresentInJavadoc(MethodDeclaration methodDeclaration, String parameterName) {
		List<JavadocBlockTag> javadocBlockTags = methodDeclaration.getJavadoc().get().getBlockTags();
		for (JavadocBlockTag javadocBlockTag : javadocBlockTags) {
			if (javadocBlockTag.getTagName().equals("param")) {
				assertThat(javadocBlockTag.getName().get()).isNotEqualTo(parameterName);
			}
		}
	}

	/**
	 * Asserts that all method calls in the body of methodWithTargetMethodCalls have
	 * the same argument size as the refactoredMethod has arguments
	 * 
	 * @param methodWithTargetMethodCalls
	 * @param refactoredMethod
	 */
	private void assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(
			MethodDeclaration methodWithTargetMethodCalls, MethodDeclaration refactoredMethod) {
		for (MethodCallExpr methodCall : methodWithTargetMethodCalls.getBody().get().findAll(MethodCallExpr.class)) {
			if (methodCall.getNameAsString().equals(refactoredMethod.getNameAsString())) {
				NodeList<Expression> callerMethodArguments = methodCall.getArguments();
				NodeList<Parameter> refactoredMethodParameters = refactoredMethod.getParameters();

				assertThat(callerMethodArguments).hasSameSizeAs(refactoredMethodParameters);
			}
		}
	}

	/**
	 * TEST HELPER METHOD ONLY. Does not work for classes with with more than one
	 * method declaration with the same name.
	 * 
	 * Finds a method in a compilation unit inside the given class or interface and
	 * with the given name.
	 * 
	 * Hint: this method is needed to find a method in a refacored compilation unit
	 * for which we do not know the line number of the method (removing javadoc
	 * comments changes the lines)
	 * 
	 * @param classOrInterfaceName
	 * @param methodName
	 * @param cu
	 * @return MethodDeclaration or null if none found
	 */
	private MethodDeclaration getMethodByName(String classOrInterfaceName, String methodName, CompilationUnit cu) {
		for (ClassOrInterfaceDeclaration clazz : cu.findAll(ClassOrInterfaceDeclaration.class)) {
			if (clazz.getNameAsString().equals(classOrInterfaceName)) {
				List<MethodDeclaration> methods = clazz.findAll(MethodDeclaration.class);
				for (MethodDeclaration method : methods) {
					if (method.getNameAsString().equals(methodName)) {
						return method;
					}
				}
			}
		}

		return null;
	}

}
