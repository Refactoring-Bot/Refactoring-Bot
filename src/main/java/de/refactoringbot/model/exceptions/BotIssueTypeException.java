package de.refactoringbot.model.exceptions;

/**
 * this exception is for the case that someone will add an invalid type
 * for a bot issue group
 */
public class BotIssueTypeException extends Exception {

		private static final long serialVersionUID = 1L;

		public BotIssueTypeException() {
		}

		public BotIssueTypeException(String message) {
				super(message);
		}

		public BotIssueTypeException(Throwable cause) {
				super(cause);
		}

		public BotIssueTypeException(String message, Throwable cause) {
				super(message, cause);
		}
}
