package de.refactoringbot.refactoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.util.ClassUtils;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.resources.refactoringhelper.TestDataClassRefactoringHelper;
import de.refactoringbot.testutils.TestUtils;

public class RefactoringHelperTest {

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	private static final String TARGET_CLASS_NAME = "TestDataClassRefactoringHelper";
	private static final String LOCAL_METHOD_SIGNATURE = "getLineOfMethod(boolean)";

	@Test
	public void testIsLocalMethodSignatureInClassOrInterface() throws FileNotFoundException {
		// arrange
		FileInputStream in = new FileInputStream(getTestResourcesFile());
		CompilationUnit cu = StaticJavaParser.parse(in);
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
		CompilationUnit cu = StaticJavaParser.parse(in);
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
		CompilationUnit cu = StaticJavaParser.parse(in);
		int lineNumber = TestDataClassRefactoringHelper.lineNumberOfFieldDeclaration;
		String expectedFieldAsString = "public static int lineNumberOfFieldDeclaration = " + lineNumber + ";";

		// act
		FieldDeclaration field = RefactoringHelper.getFieldDeclarationByLineNumber(lineNumber, cu);

		// assert
		assertThat(field).isNotNull();
		assertThat(field.toString()).isEqualTo(expectedFieldAsString);
	}

	@Test
	public void testGetClassOrInterfaceOfMethod() throws FileNotFoundException {
		// arrange
		FileInputStream in = new FileInputStream(getTestResourcesFile());
		CompilationUnit cu = StaticJavaParser.parse(in);
		int lineNumber = TestDataClassRefactoringHelper.getLineOfMethod(true);

		MethodDeclaration methodDeclaration = RefactoringHelper.getMethodDeclarationByLineNumber(lineNumber, cu);
		assertThat(methodDeclaration).isNotNull();

		// act
		ClassOrInterfaceDeclaration classOrInterface = RefactoringHelper.getClassOrInterfaceOfMethod(methodDeclaration);

		// assert
		assertThat(classOrInterface).isNotNull();
		assertThat(classOrInterface.getNameAsString()).isEqualTo(TARGET_CLASS_NAME);
	}

	@Test
	public void testGetAllClassesAndInterfacesFromFile() throws FileNotFoundException {
		// arrange
		String filePath = getTestResourcesFile().getAbsolutePath();

		// act
		List<ClassOrInterfaceDeclaration> allClassesAndInterfacesOfFile = RefactoringHelper
				.getAllClassesAndInterfacesFromFile(filePath);

		// assert
		assertThat(allClassesAndInterfacesOfFile).hasSize(2);
	}

	@Test
	public void testGetQualifiedMethodSignatureAsString() throws FileNotFoundException, BotRefactoringException {
		configureStaticJavaParserForResolving();

		// arrange
		FileInputStream in = new FileInputStream(getTestResourcesFile());
		CompilationUnit cu = StaticJavaParser.parse(in);
		int lineNumber = TestDataClassRefactoringHelper.getLineOfMethod(true);
		MethodDeclaration targetMethod = RefactoringHelper.getMethodDeclarationByLineNumber(lineNumber, cu);
		assertThat(targetMethod).isNotNull();

		// act
		String qualifiedMethodSignature = RefactoringHelper.getQualifiedMethodSignatureAsString(targetMethod);

		// assert
		assertThat(qualifiedMethodSignature).isEqualTo(
				"de.refactoringbot.resources.refactoringhelper.TestDataClassRefactoringHelper.getLineOfMethod(boolean)");
	}

	@Test
	public void testFindRelatedClassesAndInterfaces() throws BotRefactoringException, IOException {
		configureStaticJavaParserForResolving();

		// arrange
		FileInputStream in = new FileInputStream(getTestResourcesFile());
		CompilationUnit cu = StaticJavaParser.parse(in);
		Optional<ClassOrInterfaceDeclaration> targetClass = cu.getClassByName(TARGET_CLASS_NAME);
		assertThat(targetClass).isPresent();

		int lineNumber = TestDataClassRefactoringHelper.getLineOfMethod(true);
		MethodDeclaration targetMethod = RefactoringHelper.getMethodDeclarationByLineNumber(lineNumber, cu);
		assertThat(targetMethod).isNotNull();

		List<String> allJavaFiles = new ArrayList<>();
		allJavaFiles.add(getTestResourcesFile().getCanonicalPath());

		// act
		Set<String> relatedClassesAndInterfaces = RefactoringHelper.findRelatedClassesAndInterfaces(allJavaFiles,
				targetClass.get(), targetMethod);

		// assert
		// this method is already being tested indirectly in some of the refactoring
		// tests (e.g. for removing a method parameter) with a larger set of classes.
		// That's why we keep it simple here.
		assertThat(relatedClassesAndInterfaces).hasSize(2);
	}

	private void configureStaticJavaParserForResolving() {
		CombinedTypeSolver typeSolver = new CombinedTypeSolver();
		String javaRoot = TestUtils.getAbsolutePathOfTestsFolder();
		typeSolver.add(new JavaParserTypeSolver(javaRoot));
		typeSolver.add(new ReflectionTypeSolver());
		JavaSymbolSolver javaSymbolSolver = new JavaSymbolSolver(typeSolver);
		StaticJavaParser.getConfiguration().setSymbolResolver(javaSymbolSolver);
	}

	private File getTestResourcesFile() {
		String pathToTestResources = "src/test/java/"
				+ ClassUtils.convertClassNameToResourcePath(TestDataClassRefactoringHelper.class.getName()) + ".java";
		return new File(pathToTestResources);
	}
}
