package de.refactoringbot.services.wit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

/**
 * This class is responsible for anonymizing possible private data from
 * comments.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class DataAnonymizerService {

	public static final String URLEXP = "(https://|http://|ftp://|file://)?(www.)?([-a-zA-Z0-9+&@#/%=~_|!:,.;\\\\(\\\\)]+[.|/])[-a-zA-Z0-9+&@#/%?=~_|\\\\(\\\\)]+";
	public static final String TOKENEXP = "[a-z0-9]{40}";
	public static final String USEREXP = "@([\\w]+)(([\\w])|(-[\\w]))*";
	public static final String REPOEXP = "([\\w]+)(([\\w])|(-[\\w]))*([/])([\\w-_]+)";
	public static final String EMAILEXP = "(([^<>'\"()\\[\\]\\\\.,;:\\s@\"]+(\\.[^<>()\\[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))";
	
	/**
	 * This method checks a comment if it contains any possible private data and
	 * retuns an anonymized version of the data.
	 * 
	 * @param comment
	 * @return anonymizedComment
	 */
	public String anonymizeComment(String comment) {
		// Remove email addresses
		comment = anonymize(comment, EMAILEXP, "EMAIL");
		// Remove all URLs
		comment = anonymize(comment, URLEXP, "URL");
		// Remove all tagged usernames
		comment = anonymize(comment, USEREXP, "@USER");
		// Remove all Repository names
		comment = anonymize(comment, REPOEXP, "REPO");
		// Remove all words that could be access tokens
		comment = anonymize(comment, TOKENEXP, "TOKEN");

		return comment;
	}

	/**
	 * This method translates all given patterns inside a string with a fixed
	 * replacement.
	 * 
	 * @param comment
	 * @param regex
	 * @param replacement
	 * @return anonymizedComment
	 */
	private String anonymize(String comment, String regex, String replacement) {
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(comment);
		comment = matcher.replaceAll(replacement);
		return comment;
	}

}
