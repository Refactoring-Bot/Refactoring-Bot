package de.refactoringbot.model.exceptions;

/**
 * Exception if something went wrong with a sonarcloud API call
 */
public class SonarcloudAPIException extends Exception {

	private static final long serialVersionUID = 1L;

	public SonarcloudAPIException() {
	}

	public SonarcloudAPIException(String message) {
		super(message);
	}

	public SonarcloudAPIException(Throwable cause) {
		super(cause);
	}

	public SonarcloudAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	public SonarcloudAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
