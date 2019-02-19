package de.refactoringbot.refactorings;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.JavaParser;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.supportedrefactorings.ReorderModifier;
import de.refactoringbot.resources.reordermodifiers.TestDataClassReorderModifiers;

public class ReorderModifiersTest extends AbstractRefactoringTests {

	private static final Logger logger = LoggerFactory.getLogger(ReorderModifiersTest.class);

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@Override
	public Class<?> getTestResourcesClass() {
		return TestDataClassReorderModifiers.class;
	}

	@Test
	public void testReorderMethodModifiers() throws Exception {
		// arrange
		int lineOfMethod = TestDataClassReorderModifiers.getLineOfMethodWithStaticAndFinalInWrongOrder();

		// act
		File tempFile = performReorderModifiers(lineOfMethod);

		// assert
		FileInputStream in = new FileInputStream(tempFile);
		CompilationUnit cu = StaticJavaParser.parse(in);
		MethodDeclaration methodDeclarationAfterRefactoring = RefactoringHelper
				.getMethodByLineNumberOfMethodName(lineOfMethod, cu);
		assertAllModifiersInCorrectOrder(methodDeclarationAfterRefactoring.getModifiers());
	}

	@Test
	public void testReorderFieldModifiers() throws Exception {
		// arrange
		int lineNumber = TestDataClassReorderModifiers.lineNumberOfFieldWithStaticAndFinalInWrongOrder;

		// act
		File tempFile = performReorderModifiers(lineNumber);

		// assert
		FileInputStream in = new FileInputStream(tempFile);
		CompilationUnit cu = StaticJavaParser.parse(in);
		FieldDeclaration fieldDeclarationAfterRefactoring = RefactoringHelper
				.getFieldDeclarationByLineNumber(lineNumber, cu);
		assertAllModifiersInCorrectOrder(fieldDeclarationAfterRefactoring.getModifiers());
	}

	@Test
	public void testReorderMethodModifiersInCorrectOrder() throws Exception {
		exception.expect(BotRefactoringException.class);
		int lineOfMethod = TestDataClassReorderModifiers.getLineOfMethodWithAllModifiersInCorrectOrder();
		performReorderModifiers(lineOfMethod);
	}

	private File performReorderModifiers(int lineNumber) throws Exception {
		// arrange
		File tempFile = getTempCopyOfTestResourcesFile();
		BotIssue issue = new BotIssue();
		GitConfiguration gitConfig = new GitConfiguration();
		ReorderModifier refactoring = new ReorderModifier();

		gitConfig.setRepoFolder("");
		issue.setFilePath(tempFile.getAbsolutePath());
		issue.setLine(lineNumber);

		// act
		String outputMessage = refactoring.performRefactoring(issue, gitConfig);
		logger.info(outputMessage);

		return tempFile;
	}

	private boolean assertAllModifiersInCorrectOrder(List<Modifier> modifiers) {
		// Following the JLS, this is the expected order:
		ArrayList<String> modifiersInCorrectOrder = new ArrayList<>();
		modifiersInCorrectOrder.add("public");
		modifiersInCorrectOrder.add("protected");
		modifiersInCorrectOrder.add("private");
		modifiersInCorrectOrder.add("abstract");
		modifiersInCorrectOrder.add("static");
		modifiersInCorrectOrder.add("final");
		modifiersInCorrectOrder.add("transient");
		modifiersInCorrectOrder.add("volatile");
		modifiersInCorrectOrder.add("synchronized");
		modifiersInCorrectOrder.add("native");
		modifiersInCorrectOrder.add("strictfp");

		int lastIndex = 0;
		String nameOfLastModifier = "";
		for (Modifier modifier : modifiers) {
			String modifierName = StringUtils.strip(modifier.toString());
			if (!modifiersInCorrectOrder.contains(modifierName)) {
				throw new IllegalArgumentException("Unknown modifier: " + modifierName);
			}
			int listIndexOfModifier = modifiersInCorrectOrder.indexOf(modifierName);
			if (lastIndex > listIndexOfModifier) {
				throw new AssertionError("Modifier '" + modifierName + "' is expected to be declared before modifier '"
						+ nameOfLastModifier + "'");
			}
			lastIndex = listIndexOfModifier;
			nameOfLastModifier = modifierName;
		}
		return true;
	}

}
