package de.refactoringBot.controller.main;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ConsoleErrorListener;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringBot.grammar.botGrammar.BotOperationsBaseListener;
import de.refactoringBot.grammar.botGrammar.BotOperationsLexer;
import de.refactoringBot.grammar.botGrammar.BotOperationsParser;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.outputModel.botPullRequestComment.BotPullRequestComment;
import de.refactoringBot.refactoring.RefactoringOperations;

/**
 * This class performs all task that have something to do with the grammar that
 * is used to read comments of pull requests.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class GrammarController {
	
	@Autowired
	RefactoringOperations operations;
	
	// Logger
	private static final Logger logger = LoggerFactory.getLogger(GrammarController.class);

	/**
	 * This method checks if a comment has a valid bot grammar and returns if the
	 * comment is valid or not.
	 * 
	 * @param comment
	 * @return valid
	 * @throws Exception
	 */
	public Boolean checkComment(String comment) {
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
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * This mehtod translates an valid comment to a BotIssue that can be refactored.
	 * 
	 * @param comment
	 * @return issue
	 * @throws Exception 
	 */
	public BotIssue createIssueFromComment(BotPullRequestComment comment) throws Exception {
		try {
			// Create object
			BotIssue issue = new BotIssue();

			// Split comment at whitespace
			String[] commentArr = comment.getCommentBody().split(" ");

			// Add data to comment
			issue.setCommentServiceID(comment.getCommentID().toString());
			issue.setLine(comment.getPosition());
			issue.setFilePath(comment.getFilepath());

			// Add operations
			if (commentArr[1].equals("ADD")) {
				// Add annotations
				if (commentArr[2].equals("ANNOTATION")) {
					// Add override annotation
					if (commentArr[3].equals("Override")) {
						issue.setRefactoringOperation(operations.ADD_OVERRIDE_ANNOTATION);
					}
					// Add line/position
					issue.setLine(Integer.valueOf(commentArr[5]));
				}
			}

			// Reorder operations
			if (commentArr[1].equals("REORDER")) {
				// Reorder modifier operation
				if (commentArr[2].equals("MODIFIER")) {
					issue.setRefactoringOperation(operations.REORDER_MODIFIER);
				}
			}

			// Rename operations
			if (commentArr[1].equals("RENAME")) {
				// Rename method operations
				if (commentArr[2].equals("METHOD")) {
					issue.setRefactoringOperation(operations.RENAME_METHOD);
					// Set new name of the method
					issue.setRefactorString(commentArr[6]);
				}
				// Add line/position
				issue.setLine(Integer.valueOf(commentArr[4]));
			}
			
			// Remove operations
			if (commentArr[1].equals("REMOVE")) {
				// Remove method parameter
				if (commentArr[2].equals("PARAMETER")) {
					issue.setRefactoringOperation(operations.REMOVE_PARAMETER);
					// Set name of the parameter
					issue.setRefactorString(commentArr[6]);
				}
				// Add line/position
				issue.setLine(Integer.valueOf(commentArr[4]));
			}
			return issue;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Could not create a BotIssue from the comment '" + comment.getCommentBody() + "'!");
		}
	}
}
