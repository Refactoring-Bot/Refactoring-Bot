package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.EnumSet;

import org.springframework.stereotype.Component;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Modifier.Keyword;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.RefactoringImpl;

/**
 * This class is used to bring method and field modifiers into the correct
 * order, following the Java Language Specification (JLS)
 */
@Component
public class ReorderModifier implements RefactoringImpl {

	/**
	 * Reorder modifiers of a given field or method to comply with the JLS
	 */
	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws Exception {
		String filepath = gitConfig.getRepoFolder() + File.separator + issue.getFilePath();
		FileInputStream in = new FileInputStream(filepath);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(JavaParser.parse(in));

		FieldDeclaration field = RefactoringHelper.getFieldDeclarationByLineNumber(issue.getLine(), compilationUnit);
		MethodDeclaration method = RefactoringHelper.getMethodDeclarationByLineNumber(issue.getLine(),
				compilationUnit);
		boolean isModifierListUnchanged = false;
		NodeList<Modifier> modifiersInCorrectOrder;
		if (field != null) {
			modifiersInCorrectOrder = getModifiersInCorrectOrder(field.getModifiers());
			isModifierListUnchanged = field.getModifiers().equals(modifiersInCorrectOrder);
			field.setModifiers(new NodeList<Modifier>());
			field.setModifiers(modifiersInCorrectOrder);
		} else if (method != null) {
			modifiersInCorrectOrder = getModifiersInCorrectOrder(method.getModifiers());
			isModifierListUnchanged = method.getModifiers().equals(modifiersInCorrectOrder);
			method.setModifiers(new NodeList<Modifier>());
			method.setModifiers(modifiersInCorrectOrder);
		} else {
			throw new BotRefactoringException("Could not find method or field declaration at the given line!");
		}

		if (isModifierListUnchanged) {
			throw new BotRefactoringException("All modifiers are in correct order! Nothing to refactor.");
		}

		// Save changes to file
		PrintWriter out = new PrintWriter(filepath);
		out.println(LexicalPreservingPrinter.print(compilationUnit));
		out.close();

		// Return commit message
		return "Reordered modifiers to comply with the Java Language Specification";
	}

	private NodeList<Modifier> getModifiersInCorrectOrder(NodeList<Modifier> modifiers) {
		NodeList<Modifier> reorderedModifiers = new NodeList<>();

		if (modifiers.size() <= 1) {
			return modifiers;
		}

		// Fill enum set with java modifier keywords, assuming that they are in the
		// correct order
		EnumSet<Keyword> keywords = EnumSet.noneOf(Keyword.class);
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

		return reorderedModifiers;
	}

}
