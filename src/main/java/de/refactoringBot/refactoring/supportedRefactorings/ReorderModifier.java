package de.refactoringBot.refactoring.supportedRefactorings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
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
			// Create list for reverted order
			NodeList<Modifier> revertedModifiers = new NodeList<Modifier>();
			// Create list for correct ordered modifiers
			NodeList<Modifier> reorderedModifiers = new NodeList<Modifier>();
			// Init empty enumset with string list for modifiers
			EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);
			List<String> modifierString = new ArrayList<String>();

			// Fill enum set and fill reverted modifiers
			for (Modifier modifier : modifiers) {
				revertedModifiers.addFirst(modifier);
				// Keywords are correctly ordered here
				keywords.add(modifier.getKeyword());
			}

			// Iterate correctly ordered keywords
			for (Keyword keyword : keywords) {
				// For each modifier
				for (Modifier modifier : modifiers) {
					// If modifier = keyword
					if (keyword.equals(modifier.getKeyword())) {
						// Add to reordered list
						reorderedModifiers.add(modifier);
						// Add modifier string in reverted order
						modifierString.add(0, keyword.asString());
					}
				}
			}

			// If order needs to be changed
			if (!modifiers.equals(reorderedModifiers)) {
				// Trigger helper variable
				reorderingNeccessary = true;
				// Iterate reverted modifiers
				for (int i = 0; i < revertedModifiers.size(); i++) {
					// Refactor them with correct ordered modifier
					if (revertedModifiers.get(i).getBegin().isPresent()
							&& revertedModifiers.get(i).getEnd().isPresent()) {
						reorderManually(revertedModifiers.get(i).getBegin().get().line, modifierString.get(i),
								revertedModifiers.get(i).getKeyword().asString(), filepath);
					}
				}
			}
		}

		// Error if everything is in correct order
		if (!reorderingNeccessary) {
			throw new BotRefactoringException("All modifiers are in correct order! Nothing to refactor.");
		}

		return declarators;
	}

	/**
	 * This method reorders the modifiers manually since the
	 * LexicalPreservingPrinter has issues with reordering them automatically with
	 * the compulation unit in the newer version of the Javaparser (which is needed
	 * for the newest features and bugfixes)
	 * 
	 * @param lineStart
	 * @param newModifier
	 * @param oldModifier
	 * @param filePath
	 * @throws IOException
	 */
	private void reorderManually(Integer lineStart, String newModifier, String oldModifier, String filePath)
			throws IOException {

		StringBuilder sb = new StringBuilder();
		File inputFile = new File(filePath);
		
		try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
			// Default: UNIX style line endings
			System.setProperty("line.separator", "\r\n");

			String currentLine;
			Integer lineNumber = 0;

			// Iterate all line inside the javafile
			while ((currentLine = reader.readLine()) != null) {
				lineNumber++;

				// Line of Declaration
				if (lineNumber == lineStart) {
					// Reorder
					currentLine = currentLine.replaceFirst(oldModifier, newModifier);
				}

				if (lineNumber != 1) {
					sb.append(System.getProperty("line.separator"));
				}

				sb.append(currentLine);
			}

		}
		
		PrintWriter out = new PrintWriter(filePath);
		out.println(sb.toString());
		out.close();
	}
}
