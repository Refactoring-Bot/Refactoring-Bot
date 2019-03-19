package de.refactoringbot.refactorings;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
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
import de.refactoringbot.resources.removeparameter.TestDataClassRemoveParameter;
import de.refactoringbot.resources.removeparameter.TestDataClassRemoveParameter.TestDataInnerClassRemoveParameter;
import de.refactoringbot.resources.removeparameter.TestDataClassWithCallOfTargetMethod;
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

	private TestDataClassRemoveParameter removeParameterTestClass = new TestDataClassRemoveParameter();
	private TestDataInnerClassRemoveParameter removeParameterInnerTestClass = removeParameterTestClass.new TestDataInnerClassRemoveParameter();
	private TestDataClassWithCallOfTargetMethod removeParameterCallerTestClass = new TestDataClassWithCallOfTargetMethod();
	private TestDataSuperClassRemoveParameter removeParameterSuperClass = new TestDataSuperClassRemoveParameter();
	private TestDataSubClassRemoveParameter removeParameterSubClass = new TestDataSubClassRemoveParameter();
	private TestDataSiblingClassRemoveParameter removeParameterSiblingClass = new TestDataSiblingClassRemoveParameter();

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testRemoveUnusedParameter() throws Exception {
		testRemoveParameter(removeParameterTestClass.getLineOfMethodWithUnusedParameter(0, 0, 0), "b");
	}

	@Test
	public void testRemoveUsedParameter() throws Exception {
		exception.expect(BotRefactoringException.class);
		testRemoveParameter(removeParameterTestClass.getLineOfMethodWithUnusedParameter(0, 0, 0), "a");
	}

	@Test
	public void testRemoveNotExistingParameter() throws Exception {
		exception.expect(BotRefactoringException.class);
		testRemoveParameter(removeParameterTestClass.getLineOfMethodWithUnusedParameter(0, 0, 0), "d");
	}

	/**
	 * Tries to remove the parameter with the given name from the method in the
	 * given line. Checks if refactoring was successful in all test data classes
	 * 
	 * @param lineNumberOfMethodWithParameterToBeRemoved
	 * @param parameterName
	 * @throws Exception
	 */
	private void testRemoveParameter(int lineNumberOfMethodWithParameterToBeRemoved, String parameterName)
			throws Exception {
		// arrange
		File fileWithCodeSmell = createTempCopyOfTestResourcesFile(TestDataClassRemoveParameter.class);
		File fileOfSuperClass = createTempCopyOfTestResourcesFile(TestDataSuperClassRemoveParameter.class);
		File fileOfSubClass = createTempCopyOfTestResourcesFile(TestDataSubClassRemoveParameter.class);
		File fileWithCallerMethod = createTempCopyOfTestResourcesFile(TestDataClassWithCallOfTargetMethod.class);
		File fileOfSiblingClass = createTempCopyOfTestResourcesFile(TestDataSiblingClassRemoveParameter.class);

		CompilationUnit cuOriginalFileWithCodeSmell = JavaParser.parse(fileWithCodeSmell);
		CompilationUnit cuOriginalFileWithCallerMethod = JavaParser.parse(fileWithCallerMethod);
		CompilationUnit cuOriginalFileOfSuperClass = JavaParser.parse(fileOfSuperClass);
		CompilationUnit cuOriginalFileOfSubClass = JavaParser.parse(fileOfSubClass);
		CompilationUnit cuOriginalFileOfSiblingClass = JavaParser.parse(fileOfSiblingClass);

		MethodDeclaration originalMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				lineNumberOfMethodWithParameterToBeRemoved, cuOriginalFileWithCodeSmell);
		MethodDeclaration originalDummyMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterTestClass.getLineNumberOfDummyMethod(0, 0, 0), cuOriginalFileWithCodeSmell);
		MethodDeclaration originalCallerMethod = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterTestClass.getLineNumberOfCaller(), cuOriginalFileWithCodeSmell);
		MethodDeclaration originalCallerMethodInnerClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterInnerTestClass.getLineNumberOfCallerInInnerClass(), cuOriginalFileWithCodeSmell);
		MethodDeclaration originalCallerMethodInDifferentFile = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterCallerTestClass.getLineOfCallerMethodInDifferentFile(), cuOriginalFileWithCallerMethod);
		MethodDeclaration originalMethodInSuperClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSuperClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfSuperClass);
		MethodDeclaration originalMethodInSubClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSubClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfSubClass);
		MethodDeclaration originalMethodInSiblingClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSiblingClass.getLineOfMethodWithUnusedParameter(0, 0, 0), cuOriginalFileOfSiblingClass);
		MethodDeclaration originalCallerMethodInSiblingClass = RefactoringHelper.getMethodDeclarationByLineNumber(
				removeParameterSiblingClass.getLineNumberOfCaller(), cuOriginalFileOfSiblingClass);
		MethodDeclaration originalMethodWithTargetMethodSignatureInInnerClass = RefactoringHelper
				.getMethodDeclarationByLineNumber(
						removeParameterInnerTestClass.getLineOfMethodWithUnusedParameter(0, 0, 0),
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
		issue.setLine(lineNumberOfMethodWithParameterToBeRemoved);
		issue.setJavaRoots(javaRoots);
		issue.setRefactorString(parameterName);
		issue.setAllJavaFiles(Arrays.asList(fileWithCodeSmell.getCanonicalPath(), fileOfSuperClass.getCanonicalPath(),
				fileOfSubClass.getCanonicalPath(), fileWithCallerMethod.getCanonicalPath(),
				fileOfSiblingClass.getCanonicalPath()));

		// act
		RemoveMethodParameter refactoring = new RemoveMethodParameter();
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);

		// assert
		String methodName = originalMethod.getNameAsString();
		String dummyMethodName = originalDummyMethod.getNameAsString();
		String callerMethodName = originalCallerMethod.getNameAsString();
		String callerMethodInInnerClassName = originalCallerMethodInnerClass.getNameAsString();
		String callerMethodInDifferentFileName = originalCallerMethodInDifferentFile.getNameAsString();
		String callerMethodInSiblingClassName = originalCallerMethodInSiblingClass.getNameAsString();
		String methodInSuperClassName = originalMethodInSuperClass.getNameAsString();
		String methodInSubClassName = originalMethodInSubClass.getNameAsString();
		String methodInSiblingClassName = originalMethodInSiblingClass.getNameAsString();
		String methodWithTargetMethodSignatureInInnerClassName = originalMethodWithTargetMethodSignatureInInnerClass
				.getNameAsString();

		CompilationUnit cuRefactoredFileWithCodeSmell = JavaParser.parse(fileWithCodeSmell);
		CompilationUnit cuRefactoredFileWithCallerMethod = JavaParser.parse(fileWithCallerMethod);
		CompilationUnit cuRefactoredFileOfSuperClass = JavaParser.parse(fileOfSuperClass);
		CompilationUnit cuRefactoredFileOfSubClass = JavaParser.parse(fileOfSubClass);
		CompilationUnit cuRefactoredFileOfSiblingClass = JavaParser.parse(fileOfSiblingClass);

		// assert that parameter has been removed from the target method
		MethodDeclaration refactoredMethod = getMethodByName(TARGET_CLASS_NAME, methodName,
				cuRefactoredFileWithCodeSmell);
		assertThat(refactoredMethod).isNotNull();
		assertThat(refactoredMethod.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that parameter has also been removed from the javadoc
		assertParameterNotPresentInJavadoc(refactoredMethod, parameterName);

		// assert that dummy method is unchanged
		MethodDeclaration dummyMethod = getMethodByName(TARGET_CLASS_NAME, dummyMethodName,
				cuRefactoredFileWithCodeSmell);
		assertThat(dummyMethod).isNotNull();
		assertThat(dummyMethod.getParameterByName(parameterName)).isPresent();

		// assert that inner class method with same name as target method is unchanged
		MethodDeclaration methodWithTargetMethodSignatureInInnerClass = getMethodByName(TARGET_INNER_CLASS_NAME,
				methodWithTargetMethodSignatureInInnerClassName, cuRefactoredFileWithCodeSmell);
		assertThat(methodWithTargetMethodSignatureInInnerClass).isNotNull();
		assertThat(methodWithTargetMethodSignatureInInnerClass.getParameterByName(parameterName)).isPresent();

		// assert that caller method in same file has been refactored
		MethodDeclaration methodWithTargetMethodCalls = getMethodByName(TARGET_CLASS_NAME, callerMethodName,
				cuRefactoredFileWithCodeSmell);
		assertThat(methodWithTargetMethodCalls).isNotNull();
		assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(methodWithTargetMethodCalls,
				refactoredMethod);

		// assert that caller method in same file in inner class has been refactored
		MethodDeclaration methodWithTargetMethodCallInInnerClass = getMethodByName(TARGET_INNER_CLASS_NAME,
				callerMethodInInnerClassName, cuRefactoredFileWithCodeSmell);
		assertThat(methodWithTargetMethodCallInInnerClass).isNotNull();
		assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(methodWithTargetMethodCallInInnerClass,
				refactoredMethod);

		// assert that caller method in different file has been refactored
		MethodDeclaration methodInDifferentFileWithTargetMethodCalls = getMethodByName(CALL_OF_TARGET_METHOD_CLASS_NAME,
				callerMethodInDifferentFileName, cuRefactoredFileWithCallerMethod);
		assertThat(methodInDifferentFileWithTargetMethodCalls).isNotNull();
		assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(
				methodInDifferentFileWithTargetMethodCalls, refactoredMethod);

		// assert that target's super class has been refactored
		MethodDeclaration methodInSuperClass = getMethodByName(SUPER_CLASS_NAME, methodInSuperClassName,
				cuRefactoredFileOfSuperClass);
		assertThat(methodInSuperClass).isNotNull();
		assertThat(methodInSuperClass.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that target's sub class has been refactored
		MethodDeclaration methodInSubClass = getMethodByName(SUB_CLASS_NAME, methodInSubClassName,
				cuRefactoredFileOfSubClass);
		assertThat(methodInSubClass).isNotNull();
		assertThat(methodInSubClass.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that target's sibling has been refactored
		MethodDeclaration methodInSiblingClass = getMethodByName(SIBLING_CLASS_NAME, methodInSiblingClassName,
				cuRefactoredFileOfSiblingClass);
		assertThat(methodInSiblingClass).isNotNull();
		assertThat(methodInSiblingClass.getParameterByName(parameterName).isPresent()).isFalse();

		// assert that caller method in target's sibling class has been refactored
		MethodDeclaration methodInSiblingClassWithSiblingMethodCall = getMethodByName(SIBLING_CLASS_NAME,
				callerMethodInSiblingClassName, cuRefactoredFileOfSiblingClass);
		assertThat(methodInSiblingClassWithSiblingMethodCall).isNotNull();
		assertAllMethodCallsArgumentSizeEqualToRefactoredMethodParameterCount(methodInSiblingClassWithSiblingMethodCall,
				refactoredMethod);
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
