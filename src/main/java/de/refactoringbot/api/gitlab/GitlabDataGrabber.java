package de.refactoringbot.api.gitlab;

import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.GitLabAPIException;

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
	
	/**
	 * This method deletes a repository from GitLab.
	 * 
	 * @param gitConfig
	 * @throws URISyntaxException
	 * @throws GitLabAPIException
	 */
	public void deleteRepository(GitConfiguration gitConfig) throws URISyntaxException, GitLabAPIException {
		String originalRepo = gitConfig.getRepoApiLink();
		String forkRepo = gitConfig.getForkApiLink();
		// Never delete the original repository
		if (originalRepo.equals(forkRepo)) {
			return;
		}
		
		
	}
}
