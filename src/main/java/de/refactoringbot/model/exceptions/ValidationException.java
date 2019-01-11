package de.refactoringbot.model.exceptions;

/**
 * Exception if an error occurred during a validation process
 */
public class ValidationException extends Exception {

	private static final long serialVersionUID = 1L;

	public ValidationException() {
	}

	public ValidationException(String message) {
		super(message);
	}

	public ValidationException(Throwable cause) {
		super(cause);
	}

	public ValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
