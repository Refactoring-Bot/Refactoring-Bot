package de.refactoringbot.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Optional;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.ClassUtils;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import de.refactoringbot.resources.refactoringhelper.TestDataClassRefactoringHelper;

public class RefactoringHelperTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private static final String TARGET_CLASS_NAME = "TestDataClassRefactoringHelper";
	private static final String LOCAL_METHOD_SIGNATURE = "getLineOfMethod(boolean)";

	@Test
	public void testIsLocalMethodSignatureInClassOrInterface() throws FileNotFoundException {
		// arrange
		FileInputStream in = new FileInputStream(getTestResourcesFile());
		CompilationUnit cu = JavaParser.parse(in);
		Optional<ClassOrInterfaceDeclaration> clazz = cu.getClassByName(TARGET_CLASS_NAME);
		assertThat(clazz).isPresent();

		// act
		boolean actual1 = RefactoringHelper.isLocalMethodSignatureInClassOrInterface(clazz.get(),
				LOCAL_METHOD_SIGNATURE);
		boolean actual2 = RefactoringHelper.isLocalMethodSignatureInClassOrInterface(clazz.get(),
				"not-present-in-class");

		// assert
		assertThat(actual1).isTrue();
		assertThat(actual2).isFalse();
	}
	
	@Test
	public void testGetMethodByLineNumberOfMethodName() throws FileNotFoundException {
		// arrange
		FileInputStream in = new FileInputStream(getTestResourcesFile());
		CompilationUnit cu = JavaParser.parse(in);
		int lineNumber = TestDataClassRefactoringHelper.getLineOfMethod(true);

		// act
		MethodDeclaration method = RefactoringHelper.getMethodDeclarationByLineNumber(lineNumber, cu);

		// assert
		assertThat(method).isNotNull();
		assertThat(method.getDeclarationAsString()).isEqualTo("public static int getLineOfMethod(boolean parm)");

		// act
		boolean isMethodDeclarationAtLineNumber = RefactoringHelper.isMethodDeclarationAtLine(method, lineNumber);

		// assert
		assertThat(isMethodDeclarationAtLineNumber).isTrue();
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
		assertThat(field).isNotNull();
		assertThat(field.toString()).isEqualTo(expectedFieldAsString);
	}

	private File getTestResourcesFile() {
		String pathToTestResources = "src/test/java/"
				+ ClassUtils.convertClassNameToResourcePath(TestDataClassRefactoringHelper.class.getName()) + ".java";
		return new File(pathToTestResources);
	}
}
