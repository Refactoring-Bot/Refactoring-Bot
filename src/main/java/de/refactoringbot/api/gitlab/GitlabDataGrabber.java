package de.refactoringbot.api.gitlab;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.refactoringbot.configuration.BotConfiguration;

/**
 * This class communicates with the Gitlab-API.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class GitlabDataGrabber {

	@Autowired
	ObjectMapper mapper;
	@Autowired
	BotConfiguration botConfig;

	private static final Logger logger = LoggerFactory.getLogger(GitlabDataGrabber.class);

	private static final String USER_AGENT = "Mozilla/5.0";
}
