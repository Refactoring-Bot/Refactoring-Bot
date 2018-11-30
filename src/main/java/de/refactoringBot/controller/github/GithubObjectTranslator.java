package de.refactoringBot.controller.github;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringBot.api.github.GithubDataGrabber;
import de.refactoringBot.configuration.BotConfiguration;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.githubModels.pullRequest.GithubUpdateRequest;
import de.refactoringBot.model.githubModels.pullRequest.GithubPullRequest;
import de.refactoringBot.model.githubModels.pullRequest.GithubCreateRequest;
import de.refactoringBot.model.githubModels.pullRequest.GithubPullRequests;
import de.refactoringBot.model.githubModels.pullRequestComment.PullRequestComment;
import de.refactoringBot.model.githubModels.pullRequestComment.GitHubPullRequestComments;
import de.refactoringBot.model.githubModels.pullRequestComment.ReplyComment;
import de.refactoringBot.model.outputModel.botPullRequest.BotPullRequest;
import de.refactoringBot.model.outputModel.botPullRequest.BotPullRequests;
import de.refactoringBot.model.outputModel.botPullRequestComment.BotPullRequestComment;
import de.refactoringBot.model.outputModel.botPullRequestComment.BotPullRequestComments;

/**
 * This class translates all kinds of objects from GitHub to Bot-Objects
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class GithubObjectTranslator {

	@Autowired
	GithubDataGrabber grabber;
	@Autowired
	BotConfiguration botConfig;

	/**
	 * This method creates a GitConfiguration from GitHub data.
	 * 
	 * @param repo
	 * @param repoService
	 * @param analysusServiceProjectKey
	 * @param maxAmountRequests
	 * @param projectRootFolder
	 * @return
	 */
	public GitConfiguration createConfiguration(String repoName, String repoOwner, String botUsername,
			String botPassword, String botEmail, String botToken, String repoService, String analysisService,
			String analysusServiceProjectKey, Integer maxAmountRequests, String projectRootFolder) {
		// Create Configuration
		GitConfiguration config = new GitConfiguration();

		// Fill object
		config.setRepoApiLink("https://api.github.com/repos/" + repoOwner + "/" + repoName);
		config.setRepoGitLink("https://github.com/" + repoOwner + "/" + repoName + ".git");
		config.setForkApiLink("https://api.github.com/repos/" + botUsername + "/" + repoName);
		config.setForkGitLink("https://github.com/" + botUsername + "/" + repoName + ".git");
		config.setRepoName(repoName);
		config.setRepoOwner(repoOwner);
		config.setRepoService(repoService.toLowerCase());
		config.setBotName(botUsername);
		config.setBotPassword(botPassword);
		config.setBotEmail(botEmail);
		
		if (analysisService != null) {
			config.setAnalysisService(analysisService.toLowerCase());
		}

		config.setAnalysisServiceProjectKey(analysusServiceProjectKey);
		config.setMaxAmountRequests(maxAmountRequests);
		config.setBotToken(botToken);
		config.setProjectRootFolder(projectRootFolder);

		return config;
	}

	/**
	 * This method translates GitHub Pull-Requests to BotPullRequests
	 * 
	 * @param githubRequests
	 * @return translatedRequests
	 * @throws Exception
	 */
	public BotPullRequests translateRequests(GithubPullRequests githubRequests, GitConfiguration gitConfig)
			throws Exception {
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
				throw new Exception("Could not build comment URI!");
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
		createRequest.setBody("Created by " + gitConfig.getBotName() + " on " + date + ".");
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

}
