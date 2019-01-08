package de.refactoringBot.refactorings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.javadoc.JavadocBlockTag;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.exceptions.BotRefactoringException;
import de.refactoringBot.refactoring.supportedRefactorings.RemoveMethodParameter;
import de.refactoringBot.resources.TestDataClassRemoveParameter;

public class TestRemoveParameter extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(TestRemoveParameter.class);
	private TestDataClassRemoveParameter removeParameterTestClass = new TestDataClassRemoveParameter();

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

	@Test
	public void testRemoveUnusedParameter2() throws Exception {
		testRemoveParameter(removeParameterTestClass.getLineOfMethodWithUnusedParameter2(0), "a");
	}

	/**
	 * Tries to remove the parameter with the given name from the method in the
	 * given line. Checks if removal was successful and if the additional dummy
	 * method in the test class remained the same
	 * 
	 * @param lineNumberOfMethodWithParameterToBeRemoved
	 * @param parameterName
	 * @throws Exception
	 */
	private void testRemoveParameter(int lineNumberOfMethodWithParameterToBeRemoved, String parameterName)
			throws Exception {
		// arrange
		File tempFile = getTempCopyOfResourcesFile("TestDataClassRemoveParameter.java");
		BotIssue issue = new BotIssue();
		GitConfiguration gitConfig = new GitConfiguration();
		RemoveMethodParameter refactoring = new RemoveMethodParameter();
		CompilationUnit cuOriginalFile = JavaParser.parse(tempFile);
		MethodDeclaration originalMethod = getMethodByLineNumber(lineNumberOfMethodWithParameterToBeRemoved,
				cuOriginalFile);
		MethodDeclaration originalDummyMethod = getMethodByLineNumber(
				removeParameterTestClass.getLineNumberOfDummyMethod(0, 0, 0), cuOriginalFile);
		MethodDeclaration originalCallerMethod = getMethodByLineNumber(removeParameterTestClass.getLineNumberOfCaller(),
				cuOriginalFile);
		assertNotNull(originalMethod);
		assertNotNull(originalDummyMethod);
		assertNotNull(originalCallerMethod);

		gitConfig.setRepoFolder("");
		issue.setFilePath(tempFile.getAbsolutePath());
		issue.setLine(lineNumberOfMethodWithParameterToBeRemoved);
		issue.setJavaRoots(new ArrayList<String>());
		issue.setRefactorString(parameterName);
		issue.setAllJavaFiles(Arrays.asList(tempFile.getAbsolutePath()));

		// act
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);

		// assert
		String methodName = originalMethod.getNameAsString();
		String dummyMethodName = originalDummyMethod.getNameAsString();
		String callerMethodName = originalCallerMethod.getNameAsString();
		FileInputStream in = new FileInputStream(tempFile);
		CompilationUnit cu = JavaParser.parse(in);
		MethodDeclaration refactoredMethod = getMethodByName(methodName, cu);

		// assert that parameter has been removed from the target method
		assertNotNull(refactoredMethod);
		assertFalse(refactoredMethod.getParameterByName(parameterName).isPresent());

		// assert that parameter has also been removed from the javadoc
		List<JavadocBlockTag> javadocBlockTags = refactoredMethod.getJavadoc().get().getBlockTags();
		for (JavadocBlockTag javadocBlockTag : javadocBlockTags) {
			assertFalse(javadocBlockTag.getTagName().equals("param")
					&& javadocBlockTag.getName().get().equals(parameterName));
		}

		// assert that dummy method is unchanged
		MethodDeclaration dummyMethod = getMethodByName(dummyMethodName, cu);
		assertNotNull(dummyMethod);
		assertTrue(dummyMethod.getParameterByName(parameterName).isPresent());

		// assert that caller method has been refactored as well
		MethodDeclaration callerMethod = getMethodByName(callerMethodName, cu);
		assertNotNull(callerMethod);
		for (MethodCallExpr methodCall : callerMethod.getBody().get().findAll(MethodCallExpr.class)) {
			if (methodCall.getNameAsString().equals(refactoredMethod.getNameAsString())) {
				NodeList<Expression> callerMethodArguments = methodCall.getArguments();
				NodeList<Parameter> refactoredMethodParameters = refactoredMethod.getParameters();

				assertEquals(refactoredMethodParameters.size(), callerMethodArguments.size());
			}
		}

	}

	/**
	 * 
	 * @param lineNumber
	 * @param cu
	 * @return
	 */
	private MethodDeclaration getMethodByLineNumber(int lineNumber, CompilationUnit cu) {
		MethodDeclaration result = null;
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		for (MethodDeclaration method : methods) {
			if (method.getBegin().get().line == lineNumber) {
				result = method;
			}
		}
		return result;
	}

	/**
	 * 
	 * @param methodName
	 * @param cu
	 * @return
	 */
	private MethodDeclaration getMethodByName(String methodName, CompilationUnit cu) {
		MethodDeclaration result = null;
		List<MethodDeclaration> methods = cu.findAll(MethodDeclaration.class);
		for (MethodDeclaration method : methods) {
			if (method.getNameAsString().equals(methodName)) {
				result = method;
			}
		}
		return result;
	}

}
