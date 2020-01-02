package de.refactoringbot.services.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import de.refactoringbot.model.botissuegroup.BotIssueGroup;
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
import de.refactoringbot.model.github.pullrequestcomment.GitHubPullRequestComments;
import de.refactoringbot.model.github.pullrequestcomment.PullRequestComment;
import de.refactoringbot.model.github.pullrequestcomment.ReplyComment;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComments;
import de.refactoringbot.services.main.GitService;

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
	private final GitService gitService;
	private static final String PULL_REQUEST_DESCRIPTION = "Hi, I'm a refactoring bot. I found and fixed some code smells for you. \n\n You can instruct me to perform changes on this pull request by creating line specific comments inside the 'Files changed' tab of this pull request. Use the english language to give me instructions and do not forget to tag me (using @) inside the comment to let me know that you are talking to me.";

	@Autowired
	public GithubObjectTranslator(GithubDataGrabber grabber, ModelMapper modelMapper, GitService gitService) {
		this.grabber = grabber;
		this.modelMapper = modelMapper;
		this.gitService = gitService;
	}

	/**
	 * This method creates a GitConfiguration from GitHub data.
	 * 
	 * @param configuration
	 * @param apiUrl
	 * @param gitUrl
	 * @return
	 */
	public GitConfiguration createConfiguration(GitConfigurationDTO configuration, String apiUrl, String gitUrl) {

		GitConfiguration config = new GitConfiguration();

		modelMapper.map(configuration, config);
		// Fill object
		config.setRepoApiLink(apiUrl);
		config.setRepoGitLink(gitUrl + ".git");

		if (configuration.getAnalysisService() != null) {
			config.setAnalysisService(configuration.getAnalysisService());
		}

		return config;
	}

	/**
	 * This method adds the details of a fork to the GitConfiguration after the fork
	 * was created.
	 * 
	 * @param gitConfig
	 * @param apiUrl
	 * @param gitUrl
	 * @return gitConfig
	 */
	public GitConfiguration addForkDetailsToConfiguration(GitConfiguration gitConfig, String apiUrl, String gitUrl) {
		gitConfig.setForkApiLink(apiUrl);
		gitConfig.setForkGitLink(gitUrl + ".git");
		return gitConfig;
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

		BotPullRequests translatedRequests = new BotPullRequests();

		for (GithubPullRequest githubRequest : githubRequests.getAllPullRequests()) {

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

			URI commentUri = null;
			try {
				// Read comments URI
				commentUri = new URI(githubRequest.getReviewCommentsUrl());
			} catch (URISyntaxException e) {
				logger.error(e.getMessage(), e);
				throw new URISyntaxException("Could not build comment URI!", e.getMessage());
			}

			// Get and translate comments from github
			GitHubPullRequestComments githubComments = grabber.getAllPullRequestComments(commentUri, gitConfig);
			BotPullRequestComments comments = translatePullRequestComments(githubComments);
			pullRequest.setAllComments(comments.getComments());

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
		BotPullRequestComments translatedComments = new BotPullRequestComments();

		for (PullRequestComment githubComment : githubComments.getComments()) {
			BotPullRequestComment translatedComment = new BotPullRequestComment();

			// Fill comment with data
			translatedComment.setCommentID(githubComment.getId());
			translatedComment.setFilepath(githubComment.getPath());
			translatedComment.setUsername(githubComment.getUser().getLogin());
			translatedComment.setCommentBody(githubComment.getBody());
			translatedComment.setPosition(gitService.getLineNumberOfLastLineInDiffHunk(githubComment.getDiffHunk()));

			translatedComments.addComment(translatedComment);
		}

		return translatedComments;
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
		GithubCreateRequest createRequest = new GithubCreateRequest();

		// Fill object with data
		createRequest.setTitle(issue.getCommitMessage());
		createRequest.setBody(PULL_REQUEST_DESCRIPTION);
		createRequest.setHead(gitConfig.getBotName() + ":" + newBranch);
		createRequest.setBase("master");
		createRequest.setMaintainer_can_modify(true);

		return createRequest;
	}

		/**
		 * This method creates an object that can be used to create a Pull-Request on
		 * GitHub after a analysis service refactoring.
		 * This method creates Pull-Requests with the BotIssueGroup
		 *
		 * @param group
		 * @param gitConfig
		 * @param newBranch
		 * @return createRequest
		 */
		public GithubCreateRequest makeCreateRequestWithAnalysisService(BotIssueGroup group, GitConfiguration gitConfig,
				String newBranch) {
				GithubCreateRequest createRequest = new GithubCreateRequest();

				// Fill object with data
				createRequest.setTitle(group.getBotIssueGroup().get(0).getCommitMessage());
				createRequest.setBody(PULL_REQUEST_DESCRIPTION);
				createRequest.setHead(gitConfig.getBotName() + ":" + newBranch);
				createRequest.setBase("master");
				createRequest.setMaintainer_can_modify(true);

				return createRequest;
		}

	/**
	 * This method creates an object that can be used reply to a comment on GitHub.
	 * 
	 * @param replyTo
	 * @param newRequestURL
	 * @return comment
	 */
	public ReplyComment createReplyComment(BotPullRequestComment replyTo, String newRequestURL) {
		ReplyComment comment = new ReplyComment();

		comment.setIn_reply_to(replyTo.getCommentID());

		if (newRequestURL != null) {
			// If new PullRequest created
			comment.setBody("Refactoring was successful! See request " + newRequestURL + ".");
		} else {
			// If old request updated
			comment.setBody("Refactoring was successful!");
		}

		return comment;
	}

	/**
	 * This method creates a reply comment for a failed refactoring that was
	 * triggered by a comment of a pull request.
	 * 
	 * @param replyTo
	 * @param errorMessage
	 * @return replyComment
	 */
	public ReplyComment createFailureReply(BotPullRequestComment replyTo, String errorMessage) {
		ReplyComment comment = new ReplyComment();

		comment.setIn_reply_to(replyTo.getCommentID());
		comment.setBody(errorMessage);

		return comment;
	}

}
