package de.refactoringbot.model.exceptions;

/**
 * This exception should be thrown whenever our langauge processing services (Wit or ANTLR) are
 * not able to process a comment correctly.
 * 
 * @author Stefan Basaric
 *
 */
public class ReviewCommentUnclearException extends Exception {

	private static final long serialVersionUID = 1L;

	public ReviewCommentUnclearException() {
	}

	public ReviewCommentUnclearException(String message) {
		super(message);
	}

	public ReviewCommentUnclearException(Throwable cause) {
		super(cause);
	}

	public ReviewCommentUnclearException(String message, Throwable cause) {
		super(message, cause);
	}
}
