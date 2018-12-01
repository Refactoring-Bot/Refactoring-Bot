package de.refactoringBot.refactoring.supportedRefactorings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.RefactoringImpl;

/**
 * This class is used for executing the add override annotation refactoring.
 *
 * @author Timo Pfaff
 */
@Component
public class AddOverrideAnnotation implements RefactoringImpl {

	/**
	 * This method performs the refactoring and returns the a commit message.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws FileNotFoundException
	 */
	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws FileNotFoundException {

		// Prepare data
		String path = issue.getFilePath();
		String methodName = null;

		// Read file
		FileInputStream in = new FileInputStream(gitConfig.getRepoFolder() + "/" + path);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

		List<MethodDeclaration> methods = compilationUnit.findAll(MethodDeclaration.class);

		// Search all methods
		for (MethodDeclaration method : methods) {
			// If methods match
			methodName = addAnnotation(method, issue.getLine());

			// If method found
			if (methodName != null) {
				break;
			}
		}

		// If method not found
		if (methodName == null) {
			return null;
		}

		// Save changes to file
		PrintWriter out = new PrintWriter(gitConfig.getRepoFolder() + "/" + path);
		out.println(LexicalPreservingPrinter.print(compilationUnit));
		out.close();

		// Return commit message
		return "Added override annotation to method " + methodName;
	}

	/**
	 * This method adds the override annotation to a method at a specific line
	 * inside the java file.
	 * 
	 * @param declaration
	 * @param line
	 * @return methodName
	 */
	public String addAnnotation(MethodDeclaration declaration, Integer line) {
		// If method declaration = method that needs refactoring
		if (line == declaration.getName().getBegin().get().line) {
			// Add annotation
			declaration.addMarkerAnnotation("Override");

			// return method name
			return declaration.getNameAsString();
		}
		return null;
	}

}
