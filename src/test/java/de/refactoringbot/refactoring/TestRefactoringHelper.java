package de.refactoringbot.refactoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.ClassUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.resources.refactoringhelper.TestDataClassRefactoringHelper;

public class TestRefactoringHelper {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Test
	public void testCheckForDuplicatedMethodSignaturesExpectException()
			throws FileNotFoundException, BotRefactoringException {
		exception.expect(BotRefactoringException.class);

		// arrange
		String methodSignatureAsString = "getLineOfMethod(boolean)";
		List<String> javaFiles = getJavaFilePathsForTest();
		assertEquals("getLineOfMethod(boolean)", methodSignatureAsString);

		// act
		RefactoringHelper.checkForDuplicatedMethodSignatures(javaFiles, methodSignatureAsString);
	}

	@Test
	public void testCheckForDuplicatedMethodSignaturesExpectNoException()
			throws FileNotFoundException, BotRefactoringException {
		String methodSignatureAsString = "foo(int)";
		List<String> javaFiles = getJavaFilePathsForTest();
		RefactoringHelper.checkForDuplicatedMethodSignatures(javaFiles, methodSignatureAsString);

		methodSignatureAsString = "foo()";
		RefactoringHelper.checkForDuplicatedMethodSignatures(javaFiles, methodSignatureAsString);

		methodSignatureAsString = "bar(boolean)";
		RefactoringHelper.checkForDuplicatedMethodSignatures(javaFiles, methodSignatureAsString);
	}

	@Test
	public void testGetMethodByLineNumberOfMethodName() throws FileNotFoundException {
		// arrange
		FileInputStream in = new FileInputStream(getTestResourcesFile());
		CompilationUnit cu = JavaParser.parse(in);
		int lineNumber = TestDataClassRefactoringHelper.getLineOfMethod(true);

		// act
		MethodDeclaration method = RefactoringHelper.getMethodByLineNumberOfMethodName(lineNumber, cu);

		// assert
		assertTrue(method != null);
		assertEquals("public static int getLineOfMethod(boolean parm)", method.getDeclarationAsString());
		
		// act
		boolean isMethodDeclarationAtLineNumber = RefactoringHelper.isMethodDeclarationAtLine(method, lineNumber);
		
		// assert
		assertTrue(isMethodDeclarationAtLineNumber);
	}

	@Test
	public void testGetFieldDeclarationByLineNumber() throws FileNotFoundException {
		// arrange
		FileInputStream in = new FileInputStream(getTestResourcesFile());
		CompilationUnit cu = JavaParser.parse(in);
		int lineNumber = TestDataClassRefactoringHelper.lineNumberOfFieldDeclaration;
		String expectedFieldAsString = "public static int lineNumberOfFieldDeclaration = " + lineNumber + ";";

		// act
		FieldDeclaration field = RefactoringHelper.getFieldDeclarationByLineNumber(lineNumber, cu);

		// assert
		assertTrue(field != null);
		assertEquals(expectedFieldAsString, field.toString());
	}

	private List<String> getJavaFilePathsForTest() {
		List<String> javaFiles = new ArrayList<>();
		javaFiles.add(getTestResourcesFile().getAbsolutePath());
		return javaFiles;
	}

	private File getTestResourcesFile() {
		String pathToTestResources = "src/test/java/"
				+ ClassUtils.convertClassNameToResourcePath(TestDataClassRefactoringHelper.class.getName()) + ".java";
		return new File(pathToTestResources);
	}
}
