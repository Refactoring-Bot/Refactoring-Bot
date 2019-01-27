package de.refactoringbot.services.wit;

import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Service;

/**
 * This class is responsible for anonymizing possible private data from
 * comments.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class DataAnonymizer {

	public static final String URLEXP = "^(http://|https://)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.([a-z]+)$";
	public static final String TOKENEXP = "^[a-z0-9]{40}$";

	/**
	 * This method checks a comment if it contains any possible private data and
	 * retuns an anonymized version of the data.
	 * 
	 * @param comment
	 * @return anonymizedComment
	 */
	public String anonymizeComment(String comment) {
		// Remove email addresses
		comment = anonymizeEmails(comment);
		// Remove all URLs
		// comment = anonymizeURLs(comment);
		// Remove all tagged words (usually usernames)
		comment = anonymizeTaggedUser(comment);
		// Remove all words that could be access tokens
		comment = anonymizePossibleTokens(comment);

		return comment;
	}

	/**
	 * This method translates all email-addresses from a comment to the keyword
	 * 'TOKEN'.
	 * 
	 * @param comment
	 * @return commentWithoutTokens
	 */
	private String anonymizePossibleTokens(String comment) {
		// Init anonymized comment
		String anonymizedComment = "";
		// Split input comment into words
		String[] words = comment.split(" ");

		// Iterate all words
		for (String word : words) {
			// If word is some link
			if (word.matches(TOKENEXP)) {
				// Add URL Keyword to anonymized comment
				anonymizedComment = anonymizedComment.concat("TOKEN ");
			} else {
				// Copy nun URL word to anonymized comment
				anonymizedComment = anonymizedComment.concat(word + " ");
			}
		}

		// Remove " " at end of comment that was created during anonymization
		if (anonymizedComment.endsWith(" ")) {
			anonymizedComment = anonymizedComment.substring(0, anonymizedComment.length() - 1);
		}

		return anonymizedComment;
	}

	/**
	 * This method translates all email-addresses from a comment to the keyword
	 * 'EMAIL'.
	 * 
	 * @param comment
	 * @return commentWithoutEmails
	 */
	private String anonymizeEmails(String comment) {
		// Init anonymized comment
		String anonymizedComment = "";
		// Split input comment into words
		String[] words = comment.split(" ");

		// Iterate all words
		for (String word : words) {
			// If word is some link
			if (EmailValidator.getInstance().isValid(word)) {
				// Add URL Keyword to anonymized comment
				anonymizedComment = anonymizedComment.concat("EMAIL ");
			} else {
				// Copy nun URL word to anonymized comment
				anonymizedComment = anonymizedComment.concat(word + " ");
			}
		}

		// Remove " " at end of comment that was created during anonymization
		if (anonymizedComment.endsWith(" ")) {
			anonymizedComment = anonymizedComment.substring(0, anonymizedComment.length() - 1);
		}

		return anonymizedComment;
	}

	/**
	 * This method translates all URLs from a comment to the keyword 'URL'.
	 * 
	 * @param comment
	 * @return commentWithoutURLs
	 */
	private String anonymizeURLs(String comment) {
		// Init anonymized comment
		String anonymizedComment = "";
		// Split input comment into words
		String[] words = comment.split(" ");

		// Iterate all words
		for (String word : words) {
			// If word is some link
			if (word.matches(URLEXP)) {
				// Add URL Keyword to anonymized comment
				anonymizedComment = anonymizedComment.concat("URL ");
			} else {
				// Copy nun URL word to anonymized comment
				anonymizedComment = anonymizedComment.concat(word + " ");
			}
		}

		// Remove " " at end of comment that was created during anonymization
		if (anonymizedComment.endsWith(" ")) {
			anonymizedComment = anonymizedComment.substring(0, anonymizedComment.length() - 1);
		}

		return anonymizedComment;
	}

	/**
	 * This method translates all tagged words (with '@' - usualy usernames) to the
	 * Keyword 'USER'.
	 * 
	 * @param comment
	 * @return commentWithoutTaggedWords
	 */
	private String anonymizeTaggedUser(String comment) {
		// Init anonymized comment
		String anonymizedComment = "";
		// Split input comment into words
		String[] words = comment.split(" ");

		// Iterate all words
		for (String word : words) {
			// If word is some link
			if (word.startsWith("@")) {
				// Add URL Keyword to anonymized comment
				anonymizedComment = anonymizedComment.concat("@USER ");
			} else {
				// Copy nun URL word to anonymized comment
				anonymizedComment = anonymizedComment.concat(word + " ");
			}
		}

		// Remove " " at end of comment that was created during anonymization
		if (anonymizedComment.endsWith(" ")) {
			anonymizedComment = anonymizedComment.substring(0, anonymizedComment.length() - 1);
		}

		return anonymizedComment;
	}

}
