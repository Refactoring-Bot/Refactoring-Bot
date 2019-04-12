package de.refactoringbot.refactoring.supportedrefactorings;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.printer.lexicalpreservation.LexicalPreservingPrinter;

import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.refactoring.RefactoringImpl;

/**
 * This class is used for executing the removal of commented out code
 *
 * @author Justin Kissling
 */
@Component
public class RemoveCommentedOutCode implements RefactoringImpl {

	Integer line;
	BotConfiguration botConfig;
	HashMap<Integer, Comment> commentsWithLine;

	/**
	 * This method performs the refactoring and returns a commit message.
	 *
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws IOException
	 * @throws BotRefactoringException
	 */
	@Override
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig)
			throws IOException, BotRefactoringException {

		// Prepare data
		String path = gitConfig.getRepoFolder() + "/" + issue.getFilePath();

		line = issue.getLine();

		// Read file
		FileInputStream in = new FileInputStream(path);
		CompilationUnit compilationUnit = LexicalPreservingPrinter.setup(StaticJavaParser.parse(in));

		List<Comment> comments = compilationUnit.getAllContainedComments();

                // Keeping track of the start and end line of the commented out code to add it to the output string
		int startLine = line;
		int endLine = -1;

                // Going through all comments and checking if the line matches the one we're looking for
		for (Comment comment : comments) {
			if ((line >= comment.getBegin().get().line) && (line <= comment.getEnd().get().line)) {
				if (comment.isLineComment()) {
					endLine = comment.getEnd().get().line;
					comment.remove();
					// Increase the line variable to find more commented out code lines below
					line++;
				} else if (comment.isBlockComment()) {
					// The comment is a multi-line comment, so we remove the entire thing right away
					startLine = comment.getBegin().get().line;
					endLine = comment.getEnd().get().line;
					comment.remove();
					break;
				} else if (comment.isJavadocComment()) {
					throw new BotRefactoringException(
							"Found a JavaDoc comment at the indicated line. These are not removed.");
				}
			}
		}

                // We never set the endLine variable, which means we found no matching comment for the indicated line
		if (endLine == -1) {
			throw new BotRefactoringException("Commented out code line could not be found"
					+ System.getProperty("line.separator") + "Are you sure that the source code and "
					+ "SonarQube analysis are on the same branch and version?");
		}

		// Printing the output file with JavaParser
		PrintWriter out = new PrintWriter(path);
		out.println(LexicalPreservingPrinter.print(compilationUnit));
		out.close();

		// Return commit message
		return ("Removed " + (endLine - startLine + 1) + " line(s) of commented out code (line " + startLine + "-"
				+ endLine + ")");
	}

}
