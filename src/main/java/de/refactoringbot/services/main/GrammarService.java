package de.refactoringbot.services.main;

import java.util.List;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.grammar.botgrammar.BotOperationsBaseListener;
import de.refactoringbot.grammar.botgrammar.BotOperationsLexer;
import de.refactoringbot.grammar.botgrammar.BotOperationsParser;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.refactoring.RefactoringOperations;

/**
 * This class performs all task that have something to do with the grammar that
 * is used to read comments of pull requests.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class GrammarService {

	private FileService fileService;

	private static final Logger logger = LoggerFactory.getLogger(GrammarService.class);

	@Autowired
	public GrammarService(FileService fileService) {
		this.fileService = fileService;
	}

	/**
	 * This method checks if a comment is meant for the bot to understand. That is
	 * the case, if someone (not the bot himself) tags the bot inside the comment
	 * with '@Botname'.
	 * 
	 * @param comment
	 * @param gitConfig
	 * @return isBotComment
	 */
	public boolean isBotComment(BotPullRequestComment comment, GitConfiguration gitConfig) {
		return (comment.getCommentBody().contains("@" + gitConfig.getBotName())
				&& !comment.getUsername().equals(gitConfig.getBotName()));
	}

	/**
	 * This method checks if a comment has a valid bot grammar and returns if the
	 * comment is valid or not.
	 * 
	 * @param comment
	 * @return valid
	 */
	public Boolean checkComment(String comment, GitConfiguration gitConfig) {
		try {
			// Create lexer and disable console logs
			BotOperationsLexer lexer = new BotOperationsLexer(CharStreams.fromString(comment));
			lexer.removeErrorListener(ConsoleErrorListener.INSTANCE);

			// Create parser and disable console logs
			CommonTokenStream token = new CommonTokenStream(lexer);
			BotOperationsParser parser = new BotOperationsParser(token);
			parser.setBuildParseTree(true);
			parser.removeErrorListener(ConsoleErrorListener.INSTANCE);

			// Create parse tree
			ParseTree tree = parser.botCommand();
			ParseTreeWalker walker = new ParseTreeWalker();

			// Walk path tree
			BotOperationsBaseListener listener = new BotOperationsBaseListener();
			walker.walk(listener, tree);

			// Check if USERNAME-Keyword equals tagged Botname
			if (comment.split(" ").length > 1 && !comment.split(" ")[0].equals("@" + gitConfig.getBotName())) {
				return false;
			}

			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * This method translates a valid comment to a BotIssue that can be refactored.
	 * 
	 * @param comment
	 * @return issue
	 * @throws Exception
	 */
	public BotIssue createIssueFromComment(BotPullRequestComment comment, GitConfiguration gitConfig) throws Exception {
		try {
			// Create object
			BotIssue issue = new BotIssue();

			// Add data to comment
			issue.setCommentServiceID(comment.getCommentID().toString());
			issue.setLine(comment.getPosition());
			issue.setFilePath(comment.getFilepath());

			// Set all Java-Files and Java-Roots
			List<String> allJavaFiles = fileService.getAllJavaFiles(gitConfig.getRepoFolder());
			issue.setAllJavaFiles(allJavaFiles);
			issue.setJavaRoots(fileService.findJavaRoots(allJavaFiles));

			mapCommentBodyToIssue(issue, comment.getCommentBody());
			return issue;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Could not create a BotIssue from the comment '" + comment.getCommentBody() + "'!");
		}
	}

	/**
	 * This method maps the information of a comment to the issue.
	 * 
	 * @param issue
	 * @param commentBody
	 */
	private void mapCommentBodyToIssue(BotIssue issue, String commentBody) {
		// Split comment at whitespace
		String[] commentArr = commentBody.split(" ");

		// Add operations
		if (commentArr[1].equals("ADD")) {
			// Add annotations
			if (commentArr[2].equals("ANNOTATION")) {
				// Add override annotation
				if (commentArr[3].equals("Override")) {
					issue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				}
			}
		}

		// Reorder operations
		if (commentArr[1].equals("REORDER")) {
			// Reorder modifier operation
			if (commentArr[2].equals("MODIFIER")) {
				issue.setRefactoringOperation(RefactoringOperations.REORDER_MODIFIER);
			}
		}

		// Rename operations
		if (commentArr[1].equals("RENAME")) {
			// Rename method operations
			if (commentArr[2].equals("METHOD")) {
				issue.setRefactoringOperation(RefactoringOperations.RENAME_METHOD);
				// Set new name of the method
				issue.setRefactorString(commentArr[4]);
			}
		}

		// Remove operations
		if (commentArr[1].equals("REMOVE")) {
			// Remove method parameter
			if (commentArr[2].equals("PARAMETER")) {
				issue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
				// Set name of the parameter
				issue.setRefactorString(commentArr[3]);
			}
		}
	}
}
