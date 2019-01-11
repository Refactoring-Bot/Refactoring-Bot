package de.refactoringbot.model.exceptions;

/**
 * Exception for the case that a refactoring to be performed cannot be performed
 * at all or cannot be performed correctly
 */
public class BotRefactoringException extends Exception {

	private static final long serialVersionUID = 1L;

	public BotRefactoringException() {
	}

	public BotRefactoringException(String message) {
		super(message);
	}

	public BotRefactoringException(Throwable cause) {
		super(cause);
	}

	public BotRefactoringException(String message, Throwable cause) {
		super(message, cause);
	}
}
