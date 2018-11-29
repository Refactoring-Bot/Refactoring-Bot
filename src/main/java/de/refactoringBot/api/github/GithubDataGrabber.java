package de.refactoringBot.api.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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

import de.refactoringBot.configuration.BotConfiguration;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.githubModels.fork.GithubFork;
import de.refactoringBot.model.githubModels.pullRequest.GithubUpdateRequest;
import de.refactoringBot.model.githubModels.pullRequest.GithubPullRequest;
import de.refactoringBot.model.githubModels.pullRequest.GithubCreateRequest;
import de.refactoringBot.model.githubModels.pullRequest.GithubPullRequests;
import de.refactoringBot.model.githubModels.pullRequestComment.PullRequestComment;
import de.refactoringBot.model.githubModels.pullRequestComment.GitHubPullRequestComments;
import de.refactoringBot.model.githubModels.pullRequestComment.ReplyComment;
import de.refactoringBot.model.githubModels.repository.GithubRepository;
import de.refactoringBot.model.githubModels.user.GithubUser;

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

	private final String USER_AGENT = "Mozilla/5.0";

	/**
	 * This method tries to get a repository from github.
	 * 
	 * @param repoName
	 * @param repoOwner
	 * @param repoService
	 * @return {Repository-File}
	 * @throws Exception
	 */
	public void checkRepository(String repoName, String repoOwner) throws Exception {
		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("api.github.com")
				.path("/repos/" + repoOwner + "/" + repoName);

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
			throw new Exception("Repository does not exist on Github!");
		}
	}

	/**
	 * This method tries to get a user with the given token.
	 * 
	 * @param botUsername
	 * @param botToken
	 * @return
	 * @throws Exception
	 */
	public void checkGithubUser(String botUsername, String botToken) throws Exception {
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
			throw new Exception("Invalid Bot-Token!");
		}

		// Prüfe Usernamen
		if (!githubUser.getLogin().equals(botUsername)) {
			throw new Exception("Bot-User does not exist on Github!");
		}
	}

	/**
	 * This method returns all PullRequest from Github.
	 * 
	 * @return allRequests
	 * @throws Exception
	 */
	public GithubPullRequests getAllPullRequests(GitConfiguration gitConfig) throws Exception {
		// Read URI from configuration
		URI configUri = null;
		try {
			configUri = new URI(gitConfig.getRepoApiLink());
		} catch (URISyntaxException u) {
			throw new Exception("Could not read URI from configuration!");
		}

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
			throw new Exception("Could not get Pull-Requests from Github!");
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
			throw new Exception("Could not create object from Github-Request json!");
		}
	}

	/**
	 * This method returns all comments of a pull request from Github.
	 * 
	 * @return allRequests
	 * @throws Exception
	 */
	public GitHubPullRequestComments getAllPullRequestComments(URI commentsUri, GitConfiguration gitConfig)
			throws Exception {
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
			throw new Exception("Could not get pull request comments from Github!");
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
			throw new Exception("Could not create object from Github-Comment json!");
		}
	}

	/**
	 * This method updates a pull request on Github.
	 * 
	 * @param send
	 * @param gitConfig
	 * @throws Exception
	 */
	public void updatePullRequest(GithubUpdateRequest send, GitConfiguration gitConfig, Integer requestNumber)
			throws Exception {
		// Read URI from configuration
		URI configUri = null;
		try {
			configUri = new URI(gitConfig.getRepoApiLink());
		} catch (URISyntaxException u) {
			throw new Exception("Could not read URI from configuration!");
		}

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
			rest.exchange(pullsUri, HttpMethod.PATCH, new HttpEntity<GithubUpdateRequest>(send), String.class);
		} catch (RestClientException e) {
			throw new Exception("Could not update pull request!");
		}
	}

	/**
	 * This method replies to a comment on Github.
	 * 
	 * @param comment
	 * @param gitConfig
	 * @param requestNumber
	 * @throws Exception
	 */
	public void responseToBotComment(ReplyComment comment, GitConfiguration gitConfig, Integer requestNumber)
			throws Exception {
		// Read URI from configuration
		URI configUri = null;
		try {
			configUri = new URI(gitConfig.getRepoApiLink());
		} catch (URISyntaxException u) {
			throw new Exception("Could not read URI from configuration!");
		}

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath() + "/pulls/" + requestNumber + "/comments");

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI pullsUri = apiUriBuilder.build().encode().toUri();

		RestTemplate rest = new RestTemplate();

		// Send request to Github-API
		try {
			rest.exchange(pullsUri, HttpMethod.POST, new HttpEntity<ReplyComment>(comment), String.class);
		} catch (RestClientException e) {
			throw new Exception("Could not reply to Github comment!");
		}
	}

	/**
	 * This method creates a pull request on Github.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws Exception
	 */
	public GithubPullRequest createRequest(GithubCreateRequest request, GitConfiguration gitConfig) throws Exception {

		// Read URI from configuration
		URI configUri = null;
		try {
			configUri = new URI(gitConfig.getRepoApiLink());
		} catch (URISyntaxException u) {
			throw new Exception("Could not read uri from configuration!");
		}

		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme(configUri.getScheme())
				.host(configUri.getHost()).path(configUri.getPath() + "/pulls");

		apiUriBuilder.queryParam("access_token", gitConfig.getBotToken());

		URI pullsUri = apiUriBuilder.build().encode().toUri();

		RestTemplate rest = new RestTemplate();

		// Send request to the GitHub-API
		try {
			return rest.exchange(pullsUri, HttpMethod.POST, new HttpEntity<GithubCreateRequest>(request),
					GithubPullRequest.class).getBody();
		} catch (RestClientException r) {
			throw new Exception("Could not create pull request on Github!");
		}
	}

	/**
	 * This method creates a fork from a repository.
	 * 
	 * @param gitConfig
	 * @return
	 * @throws Exception
	 */
	public void createFork(GitConfiguration gitConfig) throws Exception {

		// Read URI from configuration
		URI configUri = null;
		try {
			configUri = new URI(gitConfig.getRepoApiLink());
		} catch (URISyntaxException u) {
			throw new Exception("Could not read URI from configuration!");
		}

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
			throw new Exception("Could not create fork on Github!");
		}
	}

	/**
	 * This method deletes a repository from Github.
	 * 
	 * @param gitConfiguration
	 * @throws Exception
	 */
	public void deleteRepository(GitConfiguration gitConfig) throws Exception {

		// Read URI from configuration
		URI configUri = null;
		try {
			configUri = new URI(gitConfig.getForkApiLink());
		} catch (URISyntaxException u) {
			throw new Exception("Could not read URI from configuration!");
		}

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
			throw new Exception("Could not delete repository from Github!");
		}
	}

}
