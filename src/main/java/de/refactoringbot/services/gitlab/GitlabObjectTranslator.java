package de.refactoringbot.services.gitlab;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.api.gitlab.GitlabDataGrabber;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.services.main.GitService;

/**
 * This class translates all kinds of objects from GitLab to Bot-Objects
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class GitlabObjectTranslator {
	
	private static final Logger logger = LoggerFactory.getLogger(GitlabObjectTranslator.class);
	
	private final GitlabDataGrabber grabber;
	private final ModelMapper modelMapper;
	private final GitService gitService;
	private final String pullRequestBodyComment = "Hi, I'm a refactoring bot. I found and fixed some code smells for you. \n\n You can instruct me to perform changes on this pull request by creating line specific (review) comments inside the 'Files changed' tab of this pull request. Use the english language to give me instructions and do not forget to tag me (using @) inside the comment to let me know that you are talking to me.";

	@Autowired
	public GitlabObjectTranslator(GitlabDataGrabber grabber, ModelMapper modelMapper, GitService gitService) {
		this.grabber = grabber;
		this.modelMapper = modelMapper;
		this.gitService = gitService;
	}
	
	/**
	 * This method creates a GitConfiguration from GitHub data.
	 * 
	 * @param configuration
	 * @return
	 */
	public GitConfiguration createConfiguration(GitConfigurationDTO configuration) {
		
		GitConfiguration config = new GitConfiguration();

		modelMapper.map(configuration, config);
		// Fill object
		config.setRepoApiLink(
				"https://gitlab.com/api/v4/projects/" + configuration.getRepoOwner() + "%2F" + configuration.getRepoName());
		config.setRepoGitLink(
				"https://gitlab.com/" + configuration.getRepoOwner() + "/" + configuration.getRepoName().toLowerCase() + ".git");
		config.setForkApiLink(
				"https://gitlab.com/api/v4/projects/" + configuration.getBotName() + "%2F" + configuration.getRepoName());
		config.setForkGitLink(
				"https://gitlab.com/" + configuration.getBotName() + "/" + configuration.getRepoName().toLowerCase() + ".git");

		if (configuration.getAnalysisService() != null) {
			config.setAnalysisService(configuration.getAnalysisService());
		}

		return config;
	}
}
