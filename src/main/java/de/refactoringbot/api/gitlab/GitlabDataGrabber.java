package de.refactoringbot.api.gitlab;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.exceptions.GitLabAPIException;
import de.refactoringbot.model.exceptions.ValidationException;
import de.refactoringbot.model.gitlab.repository.GitLabRepository;
import de.refactoringbot.model.gitlab.user.GitLabUser;

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
	private static final String GITLAB_SCHEME = "https";
	private static final String GITLAB_HOST = "gitlab.com";
	private static final String GITLAB_APIPATH = "api/v4/";
	private static final String TOKEN_HEADER = "Private-Token";

	/**
	 * This method tries to get a repository from github.
	 * 
	 * @param repoName
	 * @param repoOwner
	 * @param botToken
	 * @throws MalformedURLException 
	 * @throws GitHubAPIException
	 */
	public void checkRepository(String repoName, String repoOwner, String botToken) throws GitLabAPIException, MalformedURLException {
		// Build URI	
		URI gitlabURI = URI.create("https://gitlab.com/api/v4/projects/" + repoOwner + "%2F" + repoName);

		RestTemplate rest = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		headers.set(TOKEN_HEADER, botToken);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		try {
			// Send request to the GitHub-API
			rest.exchange(gitlabURI, HttpMethod.GET, entity, GitLabRepository.class).getBody();
		} catch (RestClientException e) {
			logger.error(e.getMessage(), e);
			throw new GitLabAPIException("Repository does not exist on Github or invalid Bot-Token!", e);
		}
	}

	/**
	 * This method tries to get a user with the given token.
	 * 
	 * @param botUsername
	 * @param botToken
	 * @param botEmail
	 * @return
	 * @throws Exception
	 */
	public void checkGithubUser(String botUsername, String botToken, String botEmail) throws Exception {
		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(GITLAB_SCHEME).host(GITLAB_HOST)
				.path(GITLAB_APIPATH + "user");
		
		URI gitlabURI = apiUriBuilder.build().encode().toUri();

		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		headers.set(TOKEN_HEADER, botToken);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		
		GitLabUser gitLabUser = null;
		try {
			// Send request to the GitHub-API
			gitLabUser = rest.exchange(gitlabURI, HttpMethod.GET, entity, GitLabUser.class).getBody();
		} catch (RestClientException e) {
			logger.error(e.getMessage(), e);
			throw new GitLabAPIException("Invalid Bot-Token!");
		}

		// Check if user exists and has a public email
		if (!botUsername.equals(gitLabUser.getUsername())) {
			throw new ValidationException("Bot-User does not exist on GitLab!");
		}
		if (gitLabUser.getPublicEmail() == null) {
			throw new ValidationException("Bot-User does not have a public email on GitLab!");
		}
		if (!gitLabUser.getPublicEmail().equals(botEmail)) {
			throw new ValidationException("Invalid Bot-Email!");
		}
	}
	
	/**
	 * This method creates a fork from a repository.
	 * 
	 * @param gitConfig
	 * @return
	 * @throws URISyntaxException
	 * @throws GitHubAPIException
	 */
	public void createFork(GitConfiguration gitConfig) throws URISyntaxException, GitLabAPIException {

		// Build URI
		URI forkUri = createURIFromApiLink(gitConfig.getRepoApiLink() + "/fork");

		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		headers.set(TOKEN_HEADER, gitConfig.getBotToken());
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		try {
			// Send request to the Github-API
			rest.exchange(forkUri, HttpMethod.POST, entity, GitLabRepository.class).getBody();
		} catch (RestClientException r) {
			throw new GitLabAPIException("Could not create fork on GitLab!", r);
		}
	}
	
	/**
	 * This method deletes a repository from Github.
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

		// Read URI from configuration
		URI repoUri = createURIFromApiLink(gitConfig.getForkApiLink());

		RestTemplate rest = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		headers.set(TOKEN_HEADER, gitConfig.getBotToken());
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);


		try {
			// Send request to the Github-API
			rest.exchange(repoUri, HttpMethod.DELETE, entity, String.class);
		} catch (RestClientException r) {
			throw new GitLabAPIException("Could not delete repository from GitLab!", r);
		}
	}
	
	/**
	 * Attempts to instantiate a URI object using the specified API link
	 * @param link
	 * @return uri
	 * @throws URISyntaxException
	 */
	private URI createURIFromApiLink(String link) throws URISyntaxException {
		URI result = null;
		try {
			result = new URI(link);
		} catch (URISyntaxException u) {
			logger.error(u.getMessage(), u);
			throw new URISyntaxException("Could not create URI from given API link!", u.getMessage());
		}
		return result;
	}
}
