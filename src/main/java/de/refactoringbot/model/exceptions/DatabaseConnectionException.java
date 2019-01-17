package de.refactoringbot.model.exceptions;

public class DatabaseConnectionException extends Exception {

	private static final long serialVersionUID = 1L;

	public DatabaseConnectionException() {
	}

	public DatabaseConnectionException(String message) {
		super(message);
	}

	public DatabaseConnectionException(Throwable cause) {
		super(cause);
	}

	public DatabaseConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
