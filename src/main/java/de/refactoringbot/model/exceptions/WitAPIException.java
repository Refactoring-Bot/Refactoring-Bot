package de.refactoringbot.model.exceptions;

public class WitAPIException extends Exception {

	private static final long serialVersionUID = 1L;

	public WitAPIException() {
	}

	public WitAPIException(String message) {
		super(message);
	}

	public WitAPIException(Throwable cause) {
		super(cause);
	}

	public WitAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	public WitAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
