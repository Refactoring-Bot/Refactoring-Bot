package de.refactoringbot.services.gitlab;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.api.gitlab.GitlabDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.GitLabAPIException;
import de.refactoringbot.model.gitlab.pullrequest.GitLabCreateRequest;
import de.refactoringbot.model.gitlab.pullrequest.GitLabPullRequest;
import de.refactoringbot.model.gitlab.pullrequest.GitLabPullRequests;
import de.refactoringbot.model.gitlab.pullrequestdiscussion.GitLabDiscussion;
import de.refactoringbot.model.gitlab.pullrequestdiscussion.GitLabDiscussions;
import de.refactoringbot.model.gitlab.pullrequestdiscussion.Note;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComments;

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
	private static final String PULL_REQUEST_DESCRIPTION = "Hi, I'm a refactoring bot. I found and fixed some code smells for you. \n\n You can instruct me to perform changes on this merge request by creating line specific comments inside the 'Changes' tab of this merge request. Use the english language to give me instructions and do not forget to tag me (using @) inside the comment to let me know that you are talking to me.";

	@Autowired
	public GitlabObjectTranslator(GitlabDataGrabber grabber, ModelMapper modelMapper) {
		this.grabber = grabber;
		this.modelMapper = modelMapper;
	}

	/**
	 * This method creates a GitConfiguration from GitLab data.
	 * 
	 * @param configuration
	 * @param integer
	 * @return
	 * @throws GitLabAPIException
	 * @throws MalformedURLException
	 */
	public GitConfiguration createConfiguration(GitConfigurationDTO configuration, String apiUrl, String gitUrl) {

		GitConfiguration config = new GitConfiguration();

		modelMapper.map(configuration, config);
		// Fill object
		config.setRepoApiLink(apiUrl);
		config.setRepoGitLink(gitUrl);

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
		gitConfig.setForkGitLink(gitUrl);
		return gitConfig;
	}

	/**
	 * This method translates GitLab Pull-Requests to BotPullRequests
	 * 
	 * @param gitlabRequests
	 * @param gitConfig
	 * @return botRequests @throws URISyntaxException @throws
	 *         GitLabAPIException @throws IOException @throws
	 */
	public BotPullRequests translateRequests(GitLabPullRequests gitlabRequests, GitConfiguration gitConfig)
			throws URISyntaxException, GitLabAPIException, IOException {
		BotPullRequests translatedRequests = new BotPullRequests();

		for (GitLabPullRequest gitlabRequest : gitlabRequests.getAllPullRequests()) {

			BotPullRequest pullRequest = new BotPullRequest();

			// Fill request with data
			pullRequest.setRequestName(gitlabRequest.getTitle());
			pullRequest.setRequestDescription(gitlabRequest.getDescription());
			pullRequest.setRequestNumber(gitlabRequest.getIid());
			pullRequest.setRequestLink(gitlabRequest.getWebUrl());
			pullRequest.setRequestStatus(gitlabRequest.getState());
			pullRequest.setCreatorName(gitlabRequest.getAuthor().getUsername());
			pullRequest.setDateCreated(gitlabRequest.getCreatedAt());
			pullRequest.setDateUpdated(gitlabRequest.getUpdatedAt());
			pullRequest.setBranchName(gitlabRequest.getSourceBranch());
			pullRequest.setMergeBranchName(gitlabRequest.getTargetBranch());

			URI discussionsUri = null;
			try {
				// Read comments URI
				discussionsUri = new URI("https://gitlab.com/api/v4/projects/" + gitlabRequest.getProjectId()
						+ "/merge_requests/" + gitlabRequest.getIid() + "/discussions");
			} catch (URISyntaxException e) {
				logger.error(e.getMessage(), e);
				throw new URISyntaxException("Could not build discussions URI!", e.getMessage());
			}

			// Get and translate comments from GitLab
			GitLabDiscussions gitlabDiscussions = grabber.getAllPullRequestDiscussions(discussionsUri, gitConfig);
			BotPullRequestComments comments = translatePullRequestComments(gitlabDiscussions);
			pullRequest.setAllComments(comments.getComments());

			translatedRequests.addPullRequest(pullRequest);
		}

		// Fill request with data
		return translatedRequests;
	}

	/**
	 * This method translates GitLab comments to bot comments.
	 * 
	 * @param gitlabDiscussions
	 * @return translatedComments
	 */
	private BotPullRequestComments translatePullRequestComments(GitLabDiscussions gitlabDiscussions) {
		BotPullRequestComments translatedComments = new BotPullRequestComments();

		for (GitLabDiscussion gitlabDiscussion : gitlabDiscussions.getDiscussions()) {
			for (Note gitlabNote : gitlabDiscussion.getNotes()) {
				// Work only on line comments
				if (gitlabNote.getPosition() != null) {
					BotPullRequestComment translatedComment = new BotPullRequestComment();

					// Fill comment with data
					translatedComment.setDiscussionID(gitlabDiscussion.getId());
					translatedComment.setCommentID(gitlabNote.getId());
					translatedComment.setFilepath(gitlabNote.getPosition().getNewPath());
					translatedComment.setUsername(gitlabNote.getAuthor().getUsername());
					translatedComment.setCommentBody(gitlabNote.getBody());
					translatedComment.setPosition(gitlabNote.getPosition().getNewLine());

					translatedComments.addComment(translatedComment);
				}
			}
		}

		return translatedComments;
	}

	/**
	 * This method creates an object that can be used to create a Pull-Request on
	 * GitLab after a SonarQube refactoring.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @param newBranch
	 * @return createRequest
	 */
	public GitLabCreateRequest makeCreateRequestWithAnalysisService(BotIssue issue, GitConfiguration gitConfig,
			String newBranch) {
		GitLabCreateRequest createRequest = new GitLabCreateRequest();

		// Fill object with data
		createRequest.setTitle("Bot Merge-Request Refactoring with '" + gitConfig.getAnalysisService() + "'");
		createRequest.setDescription(PULL_REQUEST_DESCRIPTION);
		createRequest.setSource_branch(newBranch);
		createRequest.setTarget_branch("master");
		createRequest.setAllow_collaboration(true);
		createRequest.setTarget_project_id(getProjectId(gitConfig.getRepoApiLink()));

		return createRequest;
	}

	/**
	 * This method returns a reply comment as a String that can be created on
	 * GitLab.
	 * 
	 * @param newRequestURL
	 * @return comment
	 */
	public String getReplyComment(String newRequestURL) {
		if (newRequestURL != null) {
			// If new PullRequest created
			return "Refactoring was successful! See request " + newRequestURL + ".";
		} else {
			// If old request updated
			return "Refactoring was successful!";
		}
	}

	/**
	 * This method reads the projectID from the api link of a repository.
	 * 
	 * @param repoApiLink
	 * @return projectId
	 */
	public String getProjectId(String repoApiLink) {
		String[] splittString = repoApiLink.split("/");
		return splittString[splittString.length - 1];
	}

}
