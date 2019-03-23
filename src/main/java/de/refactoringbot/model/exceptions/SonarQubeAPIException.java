package de.refactoringbot.model.exceptions;

/**
 * Exception if something went wrong with a SonarQube API call
 */
public class SonarQubeAPIException extends Exception {

	private static final long serialVersionUID = 1L;

	public SonarQubeAPIException() {
	}

	public SonarQubeAPIException(String message) {
		super(message);
	}

	public SonarQubeAPIException(Throwable cause) {
		super(cause);
	}

	public SonarQubeAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	public SonarQubeAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
