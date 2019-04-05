package de.refactoringbot.model.exceptions;

/**
 * Exception if something went wrong with a GitLab API call
 */
public class GitLabAPIException extends Exception {

	private static final long serialVersionUID = 1L;

	public GitLabAPIException() {
	}

	public GitLabAPIException(String message) {
		super(message);
	}

	public GitLabAPIException(Throwable cause) {
		super(cause);
	}

	public GitLabAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	public GitLabAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
}
