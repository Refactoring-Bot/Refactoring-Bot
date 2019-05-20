package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.FileInputStream;
import java.io.PrintWriter;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringHelper;
import de.refactoringbot.refactoring.RefactoringImpl;

/**
 * This class is used for executing the 'add override annotation' refactoring
 */
@Component
public class AddOverrideAnnotation implements RefactoringImpl {

	private static final String OVERRIDE_ANNOTATION_NAME = "Override";

	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws Exception {
		String path = issue.getFilePath();

		FileInputStream in = new FileInputStream(gitConfig.getRepoFolder() + "/" + path);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(in));

		MethodDeclaration methodDeclarationToModify = RefactoringHelper
				.getMethodDeclarationByLineNumber(issue.getLine(), compilationUnit);
		if (methodDeclarationToModify == null) {
			throw new BotRefactoringException("Could not find a method declaration at specified line!");
		}
		if (isOverrideAnnotationExisting(methodDeclarationToModify)) {
			throw new BotRefactoringException("Method is already annotated with 'Override'!");
		}

		methodDeclarationToModify.addMarkerAnnotation(OVERRIDE_ANNOTATION_NAME);

		// Save changes to file
		PrintWriter out = new PrintWriter(gitConfig.getRepoFolder() + "/" + path);
		out.println(LexicalPreservingPrinter.print(compilationUnit));
		out.close();

		// Return commit message
		return "Added override annotation to method '" + methodDeclarationToModify.getNameAsString() + "'";
	}

	/**
	 * @param declaration
	 * @return true if given declaration already has an @Override annotation, false
	 *         otherwise
	 */
	private boolean isOverrideAnnotationExisting(MethodDeclaration declaration) {
		List<AnnotationExpr> annotations = declaration.getAnnotations();
		for (AnnotationExpr annotation : annotations) {
			if (annotation.getNameAsString().equals(OVERRIDE_ANNOTATION_NAME)) {
				return true;
			}
		}

		return false;
	}

}
