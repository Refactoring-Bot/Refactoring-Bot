package de.refactoringbot.model.exceptions;

public class CommentUnderstandingMessage extends Exception {

	private static final long serialVersionUID = 1L;

	public CommentUnderstandingMessage() {
	}

	public CommentUnderstandingMessage(String message) {
		super(message);
	}

	public CommentUnderstandingMessage(Throwable cause) {
		super(cause);
	}

	public CommentUnderstandingMessage(String message, Throwable cause) {
		super(message, cause);
	}
}
