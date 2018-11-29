package de.refactoringBot.refactoring.supportedRefactorings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.visitor.ModifierVisitor;

import de.refactoringBot.configuration.BotConfiguration;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.RefactoringImpl;

/**
 * This class is used to execute the reorder modifier refactoring.
 * 
 * The LexicalPreservationPrinter is not used here, because there are Problems
 * when reordering the Modifiers. The Printer expects the String, that was there
 * before the Refactoring was done and therefore throws an exception. It also
 * has the same problem as the remove of the unused variable.
 *
 * @author Timo Pfaff
 */
@Component
public class ReorderModifier extends ModifierVisitor<Void> implements RefactoringImpl {

	/**
	 * This method performs the refactoring and returns the a commit message.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws FileNotFoundException
	 */
	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig, BotConfiguration botConfig) throws FileNotFoundException {
		// Get filepath
		String path = issue.getFilePath();

		// Read file
		FileInputStream in = new FileInputStream(
				botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId() + "/" + path);
		CompilationUnit compilationUnit = JavaParser.parse(in);

		// Visit place in the code that needs refactoring
		visit(compilationUnit, null);

		// Save changes to file
		PrintWriter out = new PrintWriter(
				botConfig.getBotRefactoringDirectory() + gitConfig.getConfigurationId() + "/" + path);
		out.println(compilationUnit.toString());
		out.close();

		// Return commit message
		return "Reordered modifier";
	}

	/**
	 * This method reorders the modifiers.
	 */
	public Node visit(FieldDeclaration declarator, Void args) {
		EnumSet<Modifier> modifiers = declarator.getModifiers();
		declarator.setModifiers(modifiers);
		return declarator;
	}
}
