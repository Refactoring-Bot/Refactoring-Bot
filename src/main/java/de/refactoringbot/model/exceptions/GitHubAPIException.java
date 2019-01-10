package de.refactoringbot.model.exceptions;

/**
 * Exception if something went wrong with a GitHub API call
 */
public class GitHubAPIException extends Exception {

	private static final long serialVersionUID = 1L;

	public GitHubAPIException() {
	}

	public GitHubAPIException(String message) {
		super(message);
	}

	public GitHubAPIException(Throwable cause) {
		super(cause);
	}

	public GitHubAPIException(String message, Throwable cause) {
		super(message, cause);
	}

	public GitHubAPIException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
