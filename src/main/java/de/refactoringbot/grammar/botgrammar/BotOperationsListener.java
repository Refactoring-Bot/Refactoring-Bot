// Generated from botGrammar\BotOperations.g4 by ANTLR 4.7.1
package de.refactoringbot.grammar.botgrammar;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link BotOperationsParser}.
 */
public interface BotOperationsListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link BotOperationsParser#botCommand}.
	 * @param ctx the parse tree
	 */
	void enterBotCommand(BotOperationsParser.BotCommandContext ctx);
	/**
	 * Exit a parse tree produced by {@link BotOperationsParser#botCommand}.
	 * @param ctx the parse tree
	 */
	void exitBotCommand(BotOperationsParser.BotCommandContext ctx);
}