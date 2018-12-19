package de.refactoringBot.model.exceptions;

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
