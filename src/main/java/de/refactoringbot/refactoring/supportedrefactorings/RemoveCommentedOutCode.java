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

		int startLine = line;
		int endLine = -1;

		for (Comment comment : comments) {
			if ((line >= comment.getBegin().get().line) && (line <= comment.getEnd().get().line)) {
				if (comment.isLineComment()) {
					// If it's the SonarQube line, we remove it without checking,
					// otherwise check for commented out code
					if (line.equals(issue.getLine()) || isCommentedOutCode(comment.getContent())) {
						endLine = comment.getEnd().get().line;
                                                System.out.println("Trying to remove " + comment.getContent());
						comment.remove();
						// Increase the line variable to find more commented out code lines below
						line++;
					} else {
						break;
					}
				} else if (comment.isBlockComment()) {
					// The comment is a multi-line comment, so we remove the entire thing right away
					startLine = comment.getBegin().get().line;
					endLine = comment.getEnd().get().line;
					comment.remove();
					break;
				} else if (comment.isJavadocComment()) {
					throw new BotRefactoringException(
							"Found a JavaDoc comment at the indicated line. " + "These are not removed.");
				}
			}
		}

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

	/**
	 * Since sonarqube only provides one line per comment block, this method is used
	 * to determine if other comments also contain code
	 *
	 * @param content
	 *            The comment text to be examined
	 * @return Whether or not the comment contains code
	 */
	private boolean isCommentedOutCode(String content) {
		boolean matchesMethodCall = content.matches(".+\\..+\\(.*\\)");
		boolean matchesIfWhileForSwitchStatement = content.matches("(if\\s*\\(.*)| (while\\s*\\(.*)| (for\\s*\\(.*)|"
                        + " (switch\\s*\\(.*) ");
		boolean matchesEmptyLinesOrLinesEndingWithSemicolon = (content.trim().endsWith(";"))
				|| content.trim().equals("");
		boolean matchesSingleBrackets = (content.trim().equals("{")) || content.trim().equals("}");
                boolean matchesSwitchCase = content.matches("\\s*case.*:\\s*| \\s*default\\s*:\\s*");
                
		return matchesMethodCall || matchesIfWhileForSwitchStatement || matchesEmptyLinesOrLinesEndingWithSemicolon
				|| matchesSingleBrackets || matchesSwitchCase;
	}

}
