package de.refactoringbot.api.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.exceptions.ValidationException;
import de.refactoringbot.model.github.fork.GithubFork;
import de.refactoringbot.model.github.pullrequest.GithubCreateRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequests;
import de.refactoringbot.model.github.pullrequest.GithubUpdateRequest;
import de.refactoringbot.model.github.pullrequestcomment.GitHubPullRequestComments;
import de.refactoringbot.model.github.pullrequestcomment.PullRequestComment;
import de.refactoringbot.model.github.pullrequestcomment.ReplyComment;
import de.refactoringbot.model.github.repository.GithubRepository;
import de.refactoringbot.model.github.user.GithubUser;

/**
 * This class communicates with the Github-API.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class GithubDataGrabber {

	@Autowired
	ObjectMapper mapper;
	@Autowired
	BotConfiguration botConfig;

	private static final Logger logger = LoggerFactory.getLogger(GithubDataGrabber.class);

	private static final String USER_AGENT = "Mozilla/5.0";

	/**
	 * This method tries to get a repository from github.
	 * 
	 * @param repoName
	 * @param repoOwner
	 * @param botToken
	 * @return {Repository-File}
	 * @throws GitHubAPIException
	 */
	public void checkRepository(String repoName, String repoOwner, String botToken) throws GitHubAPIException {
		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("api.github.com")
				.path("/repos/" + repoOwner + "/" + repoName);

		apiUriBuilder.queryParam("access_token", botToken);

		URI githubURI = apiUriBuilder.build().encode().toUri();

		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Baue Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		// Send request to the GitHub-API
		try {
			rest.exchange(githubURI, HttpMethod.GET, entity, GithubRepository.class).getBody();
		} catch (RestClientException e) {
			logger.error(e.getMessage(), e);
			throw new GitHubAPIException("Repository does not exist on Github or invalid Bot-Token!", e);
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
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("api.github.com")
				.path("/user");

		apiUriBuilder.queryParam("access_token", botToken);

		URI githubURI = apiUriBuilder.build().encode().toUri();

		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Build Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		// Send request to the GitHub-API
		GithubUser githubUser = null;
		try {
			githubUser = rest.exchange(githubURI, HttpMethod.GET, entity, GithubUser.class).getBody();
		} catch (RestClientException e) {
			logger.error(e.getMessage(), e);
			throw new GitHubAPIException("Invalid Bot-Token!");
		}

		// Check if user exists and has a public email
		if (!botUsername.equals(githubUser.getLogin())) {
			throw new ValidationException("Bot-User does not exist on Github!");
		}
		if (githubUser.getEmail() == null) {
			throw new ValidationException("Bot-User does not have a public email on Github!");
		}
		if (!githubUser.getEmail().equals(botEmail)) {
			throw new ValidationException("Invalid Bot-Email!");
		}
	}

	/**
	 * This method checks if a branch with a specific name exists on the fork. If
	 * such a branch exists, the method throws an exception.
	 * 
	 * @param gitConfig
	 * @param branchName
	 * @throws URISyntaxException
	 * @throws BotRefactoringException
	 * @throws GitHubAPIException
	 */
	public void checkBranch(GitConfiguration gitConfig, String branchName)
			throws URISyntaxException, BotRefactoringException, GitHubAPIException {
		// Read URI from configuration
		URI configUri = createURIFromApiLink(gitConfig.getForkApiLink());

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath() + "/branches/" + branchName);

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI pullsUri = apiUriBuilder.build().encode().toUri();
		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Create Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		// Send Request to the GitHub-API
		try {
			// If branch found -> error
			rest.exchange(pullsUri, HttpMethod.GET, entity, String.class).getBody();
			throw new BotRefactoringException(
					"Issue was already refactored in the past! The bot database might have been resetted but not the fork itself.");
		} catch (RestClientException e) {
			// If branch does not exist -> return
			if (e.getMessage().equals("404 Not Found")) {
				return;
			}
			logger.error(e.getMessage(), e);
			throw new GitHubAPIException("Could not get Branch from Github!", e);
		}
	}

	/**
	 * This method returns all PullRequest from Github.
	 * 
	 * @return allRequests
	 * @throws URISyntaxException
	 * @throws GitHubAPIException
	 * @throws IOException
	 */
	public GithubPullRequests getAllPullRequests(GitConfiguration gitConfig)
			throws URISyntaxException, GitHubAPIException, IOException {
		// Read URI from configuration
		URI configUri = createURIFromApiLink(gitConfig.getRepoApiLink());

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath() + "/pulls");

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI pullsUri = apiUriBuilder.build().encode().toUri();
		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Create Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		// Send Request to the GitHub-API
		String json = null;
		try {
			json = rest.exchange(pullsUri, HttpMethod.GET, entity, String.class).getBody();
		} catch (RestClientException e) {
			logger.error(e.getMessage(), e);
			throw new GitHubAPIException("Could not get Pull-Requests from Github!", e);
		}

		// Create request object
		GithubPullRequests allRequests = new GithubPullRequests();

		// Try to map json to object
		try {
			List<GithubPullRequest> requestList = mapper.readValue(json,
					mapper.getTypeFactory().constructCollectionType(List.class, GithubPullRequest.class));
			allRequests.setAllPullRequests(requestList);
			return allRequests;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new IOException("Could not create object from Github-Request json!", e);
		}
	}

	/**
	 * This method returns all comments of a pull request from Github.
	 * 
	 * @return allRequests
	 * @throws GitHubAPIException
	 * @throws IOException
	 */
	public GitHubPullRequestComments getAllPullRequestComments(URI commentsUri, GitConfiguration gitConfig)
			throws GitHubAPIException, IOException {
		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(commentsUri.getScheme())
				.host(commentsUri.getHost()).path(commentsUri.getPath());

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI githubURI = apiUriBuilder.build().encode().toUri();
		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Create Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		// Send request to the GitHub-API
		String json = null;
		try {
			json = rest.exchange(githubURI, HttpMethod.GET, entity, String.class).getBody();
		} catch (RestClientException r) {
			throw new GitHubAPIException("Could not get pull request comments from Github!", r);
		}

		// Create comments object
		GitHubPullRequestComments allComments = new GitHubPullRequestComments();

		// Try to map json to object
		try {
			List<PullRequestComment> commentList = mapper.readValue(json,
					mapper.getTypeFactory().constructCollectionType(List.class, PullRequestComment.class));
			allComments.setComments(commentList);
			return allComments;
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new IOException("Could not create object from Github-Comment json!", e);
		}
	}

	/**
	 * This method updates a pull request on Github.
	 * 
	 * @param send
	 * @param gitConfig
	 * @throws GitHubAPIException
	 * @throws URISyntaxException
	 */
	public void updatePullRequest(GithubUpdateRequest send, GitConfiguration gitConfig, Integer requestNumber)
			throws GitHubAPIException, URISyntaxException {
		// Read URI from configuration
		URI configUri = createURIFromApiLink(gitConfig.getRepoApiLink());

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath() + "/pulls/" + requestNumber);

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI pullsUri = apiUriBuilder.build().encode().toUri();

		// For PATCH-Requests
		HttpHeaders headers = new HttpHeaders();
		MediaType mediaType = new MediaType("application", "merge-patch+json");
		headers.setContentType(mediaType);

		// Create REST-Template
		HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
		RestTemplate rest = new RestTemplate(requestFactory);

		// Send request to the GitHub-API
		try {
			rest.exchange(pullsUri, HttpMethod.PATCH, new HttpEntity<>(send), String.class);
		} catch (RestClientException e) {
			throw new GitHubAPIException("Could not update pull request!", e);
		}
	}

	/**
	 * This method replies to a comment on Github.
	 * 
	 * @param comment
	 * @param gitConfig
	 * @param requestNumber
	 * @throws URISyntaxException
	 * @throws GitHubAPIException
	 */
	public void responseToBotComment(ReplyComment comment, GitConfiguration gitConfig, Integer requestNumber)
			throws URISyntaxException, GitHubAPIException {
		// Read URI from configuration
		URI configUri = createURIFromApiLink(gitConfig.getRepoApiLink());

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath() + "/pulls/" + requestNumber + "/comments");

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI pullsUri = apiUriBuilder.build().encode().toUri();

		RestTemplate rest = new RestTemplate();

		// Send request to Github-API
		try {
			rest.exchange(pullsUri, HttpMethod.POST, new HttpEntity<>(comment), String.class);
		} catch (RestClientException e) {
			throw new GitHubAPIException("Could not reply to Github comment!", e);
		}
	}

	/**
	 * This method creates a pull request on Github.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws URISyntaxException
	 * @throws GitHubAPIException
	 */
	public GithubPullRequest createRequest(GithubCreateRequest request, GitConfiguration gitConfig)
			throws URISyntaxException, GitHubAPIException {

		// Read URI from configuration
		URI configUri = createURIFromApiLink(gitConfig.getRepoApiLink());

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath() + "/pulls");

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI pullsUri = apiUriBuilder.build().encode().toUri();

		RestTemplate rest = new RestTemplate();

		// Send request to the GitHub-API
		try {
			return rest.exchange(pullsUri, HttpMethod.POST, new HttpEntity<>(request), GithubPullRequest.class)
					.getBody();
		} catch (RestClientException r) {
			throw new GitHubAPIException("Could not create pull request on Github!", r);
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
	public void createFork(GitConfiguration gitConfig) throws URISyntaxException, GitHubAPIException {

		// Read URI from configuration
		URI configUri = createURIFromApiLink(gitConfig.getRepoApiLink());

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath() + "/forks");

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI forksUri = apiUriBuilder.build().encode().toUri();

		// Create REST-Template
		RestTemplate rest = new RestTemplate();

		// Send request to the Github-API
		try {
			rest.exchange(forksUri, HttpMethod.POST, null, GithubFork.class).getBody();
		} catch (RestClientException r) {
			throw new GitHubAPIException("Could not create fork on Github!", r);
		}
	}

	/**
	 * This method deletes a repository from Github.
	 * 
	 * @param gitConfig
	 * @throws URISyntaxException
	 * @throws GitHubAPIException
	 */
	public void deleteRepository(GitConfiguration gitConfig) throws URISyntaxException, GitHubAPIException {
		String originalRepo = gitConfig.getRepoApiLink();
		String forkRepo = gitConfig.getForkApiLink();
		// never delete the original repository
		if (originalRepo.equals(forkRepo)) {
			return;
		}

		// Read URI from configuration
		URI configUri = createURIFromApiLink(gitConfig.getForkApiLink());

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath());

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI repoUri = apiUriBuilder.build().encode().toUri();

		// Create REST-Template
		RestTemplate rest = new RestTemplate();

		// Send request to the Github-API
		try {
			rest.exchange(repoUri, HttpMethod.DELETE, null, String.class);
		} catch (RestClientException r) {
			throw new GitHubAPIException("Could not delete repository from Github!", r);
		}
	}
	
	/**
	 * Attempts to instantiate a URI object using the specified API link
	 * @param link
	 * @return
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
