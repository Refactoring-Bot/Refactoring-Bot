package de.BA.refactoringBot.refactoring.supportedRefactorings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import de.BA.refactoringBot.configuration.BotConfiguration;
import de.BA.refactoringBot.model.botIssue.BotIssue;
import de.BA.refactoringBot.model.configuration.GitConfiguration;

/**
 * This class is used for executing the add override annotation refactoring.
 *
 * @author Timo Pfaff
 */
@Component
public class AddOverrideAnnotation extends VoidVisitorAdapter<Object> {

	Integer line;
	String methodName;

	@Autowired
	BotConfiguration botConfig;

	/**
	 * This method performs the refactoring and returns the a commit message.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws FileNotFoundException
	 */
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws FileNotFoundException {
		// Prepare data
		String path = issue.getFilePath();
		line = issue.getLine();

		// Read file
		FileInputStream in = new FileInputStream(
				botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId() + "/" + path);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

		// Visit place in the code that needs refactoring
		visit(compilationUnit, null);

		// Save changes to file
		PrintWriter out = new PrintWriter(
				botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId() + "/" + path);
		out.println(LexicalPreservingPrinter.print(compilationUnit));
		out.close();

		// Return commit message
		return "Added override annotation to method " + methodName;
	}

	/**
	 * This method adds the annotation to a method at a specific line in the code.
	 * 
	 * @param declaration
	 * @param line
	 */
	public void visit(MethodDeclaration declaration, Object arg) {
		// If method exists at given line
		if (line == declaration.getName().getBegin().get().line) {
			// Read method name
			methodName = declaration.getNameAsString();
			// Add annotation
			declaration.addMarkerAnnotation("Override");
		}
	}

}
