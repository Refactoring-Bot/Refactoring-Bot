package de.refactoringbot.services.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.api.github.GithubDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.github.pullrequest.GithubCreateRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequests;
import de.refactoringbot.model.github.pullrequest.GithubUpdateRequest;
import de.refactoringbot.model.github.pullrequestcomment.GitHubPullRequestComments;
import de.refactoringbot.model.github.pullrequestcomment.PullRequestComment;
import de.refactoringbot.model.github.pullrequestcomment.ReplyComment;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComments;

/**
 * This class translates all kinds of objects from GitHub to Bot-Objects
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class GithubObjectTranslator {

	private static final Logger logger = LoggerFactory.getLogger(GithubObjectTranslator.class);

	private final GithubDataGrabber grabber;
	private final ModelMapper modelMapper;

	@Autowired
	public GithubObjectTranslator(GithubDataGrabber grabber, ModelMapper modelMapper) {
		this.grabber = grabber;
		this.modelMapper = modelMapper;
	}

	/**
	 * This method creates a GitConfiguration from GitHub data.
	 * 
	 * @param configuration
	 * @return
	 */
	public GitConfiguration createConfiguration(GitConfigurationDTO configuration) {
		// Create Configuration
		GitConfiguration config = new GitConfiguration();

		modelMapper.map(configuration, config);
		// Fill object
		config.setRepoApiLink(
				"https://api.github.com/repos/" + configuration.getRepoOwner() + "/" + configuration.getRepoName());
		config.setRepoGitLink(
				"https://github.com/" + configuration.getRepoOwner() + "/" + configuration.getRepoName() + ".git");
		config.setForkApiLink(
				"https://api.github.com/repos/" + configuration.getBotName() + "/" + configuration.getRepoName());
		config.setForkGitLink(
				"https://github.com/" + configuration.getBotName() + "/" + configuration.getRepoName() + ".git");

		if (configuration.getAnalysisService() != null) {
			config.setAnalysisService(configuration.getAnalysisService().toLowerCase());
		}

		return config;
	}

	/**
	 * This method translates GitHub Pull-Requests to BotPullRequests
	 * 
	 * @param githubRequests
	 * @return translatedRequests
	 * @throws URISyntaxException
	 * @throws IOException
	 * @throws GitHubAPIException
	 */
	public BotPullRequests translateRequests(GithubPullRequests githubRequests, GitConfiguration gitConfig)
			throws URISyntaxException, GitHubAPIException, IOException {
		// Create Requests
		BotPullRequests translatedRequests = new BotPullRequests();

		// Iterate all GitHub requests
		for (GithubPullRequest githubRequest : githubRequests.getAllPullRequests()) {
			// Create BotPullRequest
			BotPullRequest pullRequest = new BotPullRequest();

			// Fill request with data
			pullRequest.setRequestName(githubRequest.getTitle());
			pullRequest.setRequestDescription(githubRequest.getBody());
			pullRequest.setRequestNumber(githubRequest.getNumber());
			pullRequest.setRequestLink(githubRequest.getHtmlUrl());
			pullRequest.setRequestStatus(githubRequest.getState());
			pullRequest.setCreatorName(githubRequest.getUser().getLogin());
			pullRequest.setDateCreated(githubRequest.getCreatedAt());
			pullRequest.setDateUpdated(githubRequest.getUpdatedAt());
			pullRequest.setBranchName(githubRequest.getHead().getRef());
			pullRequest.setBranchCreator(githubRequest.getHead().getUser().getLogin());
			pullRequest.setMergeBranchName(githubRequest.getBase().getRef());
			pullRequest.setRepoName(githubRequest.getBase().getRepo().getFullName());

			// Create URI for the comments of the request
			URI commentUri = null;
			try {
				commentUri = new URI(githubRequest.getReviewCommentsUrl());
			} catch (URISyntaxException e) {
				logger.error(e.getMessage(), e);
				throw new URISyntaxException("Could not build comment URI!", e.getMessage());
			}

			// Get comments from github
			GitHubPullRequestComments githubComments = grabber.getAllPullRequestComments(commentUri, gitConfig);
			// Translate and add them to list
			BotPullRequestComments comments = translatePullRequestComments(githubComments);
			pullRequest.setAllComments(comments.getComments());

			// Add request to translated request list
			translatedRequests.addPullRequest(pullRequest);
		}

		return translatedRequests;
	}

	/**
	 * This method translates github comments to bot comments.
	 * 
	 * @param githubComments
	 * @return translatedComments
	 */
	public BotPullRequestComments translatePullRequestComments(GitHubPullRequestComments githubComments) {
		// Create Bot comments
		BotPullRequestComments translatedComments = new BotPullRequestComments();

		// Iterate github comments
		for (PullRequestComment githubComment : githubComments.getComments()) {
			// Create bot comment
			BotPullRequestComment translatedComment = new BotPullRequestComment();

			// Fill comment with data
			translatedComment.setCommentID(githubComment.getId());
			translatedComment.setFilepath(githubComment.getPath());
			translatedComment.setUsername(githubComment.getUser().getLogin());
			translatedComment.setCommentBody(githubComment.getBody());

			// Add comment to list
			translatedComments.addComment(translatedComment);
		}

		return translatedComments;
	}

	/**
	 * This method creates an Object that can be used to update a Pull-Request on
	 * GitHub.
	 * 
	 * @param refactoredRequest
	 * @return sendRequest
	 */
	public GithubUpdateRequest makeUpdateRequest(BotPullRequest refactoredRequest, GitConfiguration gitConfig) {
		// Create object
		GithubUpdateRequest sendRequest = new GithubUpdateRequest();

		// Create timestamp
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		Date now = new Date();
		String date = sdf.format(now);

		// Fill object with data
		sendRequest.setBody("Updated by " + gitConfig.getBotName() + " on " + date + ".");
		sendRequest.setMaintainer_can_modify(true);

		return sendRequest;
	}

	/**
	 * This method creates an object that can be used to create a Pull-Request on
	 * GitHub after a request comment refactoring.
	 * 
	 * @param gitConfig
	 * @return createRequest
	 */
	public GithubCreateRequest makeCreateRequest(BotPullRequest refactoredRequest, GitConfiguration gitConfig,
			String botBranchName) {
		// Create object
		GithubCreateRequest createRequest = new GithubCreateRequest();

		// Create timestamp
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		Date now = new Date();
		String date = sdf.format(now);

		// Fill object with data
		createRequest.setTitle("Bot Pull-Request Refactoring for PullRequest #" + refactoredRequest.getRequestNumber());
		createRequest.setBody("Created by " + gitConfig.getBotName() + " on " + date + " for PullRequest "
				+ refactoredRequest.getRequestLink() + ".");
		createRequest.setHead(gitConfig.getBotName() + ":" + botBranchName);
		createRequest.setBase(refactoredRequest.getBranchName());
		createRequest.setMaintainer_can_modify(true);

		return createRequest;
	}

	/**
	 * This method creates an object that can be used to create a Pull-Request on
	 * GitHub after a analysis service refactoring.
	 * 
	 * @param gitConfig
	 * @param newBranch
	 * @return createRequest
	 */
	public GithubCreateRequest makeCreateRequestWithAnalysisService(BotIssue issue, GitConfiguration gitConfig,
			String newBranch) {
		// Create object
		GithubCreateRequest createRequest = new GithubCreateRequest();

		// Create timestamp
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		Date now = new Date();
		String date = sdf.format(now);

		// Fill object with data
		// TODO: Dynamic branches
		createRequest.setTitle("Bot Pull-Request Refactoring with '" + gitConfig.getAnalysisService() + "'");
		createRequest.setBody("Created by " + gitConfig.getBotName() + " on " + date + " for the "
				+ gitConfig.getAnalysisService() + "-Issue '" + issue.getCommentServiceID() + "'.");
		createRequest.setHead(gitConfig.getBotName() + ":" + newBranch);
		createRequest.setBase("master");
		createRequest.setMaintainer_can_modify(true);

		return createRequest;
	}

	/**
	 * This method creates an object that can be used reply to a comment on GitHub.
	 * 
	 * @param replyTo
	 * @return comment
	 */
	public ReplyComment createReplyComment(BotPullRequestComment replyTo, GitConfiguration gitConfig,
			String newRequestURL) {
		// Create objcet
		ReplyComment comment = new ReplyComment();

		// Fill with data
		comment.setIn_reply_to(replyTo.getCommentID());

		// Create timestamp
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		Date now = new Date();
		String date = sdf.format(now);

		// Create response
		if (newRequestURL != null) {
			// If new PullRequest created
			comment.setBody(
					"Refactored by " + gitConfig.getBotName() + " on " + date + ". See request " + newRequestURL + ".");
		} else {
			// If old request updated
			comment.setBody("Refactored by " + gitConfig.getBotName() + " on " + date + ".");
		}

		return comment;
	}

	/**
	 * This method creates a reply comment for a failed refactoring that was
	 * triggered by a comment of a pull request.
	 * 
	 * @param replyTo
	 * @param errorMessage
	 * @return
	 */
	public ReplyComment createFailureReply(BotPullRequestComment replyTo, String errorMessage) {
		// Create objcet
		ReplyComment comment = new ReplyComment();

		// Fill with data
		comment.setIn_reply_to(replyTo.getCommentID());

		// Set error message
		comment.setBody(errorMessage);

		return comment;
	}

}
