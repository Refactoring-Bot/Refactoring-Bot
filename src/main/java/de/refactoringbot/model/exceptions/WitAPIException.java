package de.refactoringbot.model.exceptions;

/**
 * This exception should be thrown if an error occurs when connecting to the
 * Wit-API. Examples for this would be if Wit.ai is down or if the token is not
 * valid anymore.
 * 
 * @author Stefan Basaric
 *
 */
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
