package de.refactoringbot.model.exceptions;

/**
 * Exception if something went wrong with an action related to the git workflow
 */
public class GitWorkflowException extends Exception {

	private static final long serialVersionUID = 1L;

	public GitWorkflowException() {
	}

	public GitWorkflowException(String message) {
		super(message);
	}

	public GitWorkflowException(Throwable cause) {
		super(cause);
	}

	public GitWorkflowException(String message, Throwable cause) {
		super(message, cause);
	}

	public GitWorkflowException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

}
