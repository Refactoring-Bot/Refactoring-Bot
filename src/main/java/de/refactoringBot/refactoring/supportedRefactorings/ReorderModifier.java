package de.refactoringBot.refactoring.supportedRefactorings;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.exceptions.BotRefactoringException;
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
public class ReorderModifier implements RefactoringImpl {

	/**
	 * This method performs the refactoring and returns the a commit message.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws FileNotFoundException
	 */
	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws Exception {
		// Get filepath
		String filepath = gitConfig.getRepoFolder() + "/" + issue.getFilePath();

		// Read file
		FileInputStream in = new FileInputStream(filepath);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

		// Visit place in the code that needs refactoring
		List<FieldDeclaration> declarators = compilationUnit.findAll(FieldDeclaration.class);

		// Perform reordering
		performReordering(declarators, filepath);

		// Save changes to file
		PrintWriter out = new PrintWriter(filepath);
		out.println(LexicalPreservingPrinter.print(compilationUnit));
		out.close();

		// Return commit message
		return "Reordered modifier";
	}

	/**
	 * This method reorders all modifiers that need reordering and throws an
	 * exception if there is nothing to reorder.
	 * 
	 * @param declarators
	 * @param filepath
	 * @throws BotRefactoringException
	 * @throws IOException
	 */
	private List<FieldDeclaration> performReordering(List<FieldDeclaration> declarators, String filepath)
			throws Exception {

		// Helper variable
		boolean reorderingNeccessary = false;

		// Iterate all declarators
		for (FieldDeclaration declarator : declarators) {
			// Get modifiers
			NodeList<Modifier> modifiers = declarator.getModifiers();
			
			// If no or one modifier -> no reordering
			if (modifiers.size() <= 1) {
				continue;
			}
			
			// Reorderd modifiers
			NodeList<Modifier> reorderedModifiers = new NodeList<Modifier>();
			// Init empty enumset
			EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);

			// Fill enum set
			for (Modifier modifier : modifiers) {
				keywords.add(modifier.getKeyword());
			}

			// Reorder modifiers
			for (Keyword keyword : keywords) {
				for (Modifier modifier : modifiers) {
					if (keyword.equals(modifier.getKeyword())) {
						reorderedModifiers.add(modifier);
					}
				}
			}

			// Trigger helper variable
			if (!modifiers.equals(reorderedModifiers)) {
				reorderingNeccessary = true;
			}

			// Delete all modifiers
			declarator.setModifiers(new NodeList<Modifier>());

			// Add them in correct order
			for (Modifier modifier : reorderedModifiers) {
				declarator.addModifier(modifier.getKeyword());
			}
		}

		// Error if everything is in correct order
		if (!reorderingNeccessary) {
			throw new BotRefactoringException("All modifiers are in correct order! Nothing to refactor.");
		}

		return declarators;
	}
}
