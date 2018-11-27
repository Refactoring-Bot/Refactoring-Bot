package de.BA.refactoringBot.api.main;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.BA.refactoringBot.api.github.GithubDataGrabber;
import de.BA.refactoringBot.api.sonarCube.SonarCubeDataGrabber;
import de.BA.refactoringBot.controller.github.GithubObjectTranslator;
import de.BA.refactoringBot.controller.main.BotController;
import de.BA.refactoringBot.controller.sonarCube.SonarCubeObjectTranslator;
import de.BA.refactoringBot.model.botIssue.BotIssue;
import de.BA.refactoringBot.model.configuration.GitConfiguration;
import de.BA.refactoringBot.model.githubModels.pullRequest.GithubCreateRequest;
import de.BA.refactoringBot.model.githubModels.pullRequest.GithubPullRequest;
import de.BA.refactoringBot.model.githubModels.pullRequest.GithubPullRequests;
import de.BA.refactoringBot.model.githubModels.pullRequest.GithubUpdateRequest;
import de.BA.refactoringBot.model.outputModel.botPullRequest.BotPullRequest;
import de.BA.refactoringBot.model.outputModel.botPullRequest.BotPullRequests;
import de.BA.refactoringBot.model.outputModel.botPullRequestComment.BotPullRequestComment;
import de.BA.refactoringBot.model.sonarQube.SonarCubeIssues;

/**
 * This class transfers all Rest-Requests to correct APIs and returns all
 * objects as translated bot objects.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class ApiGrabber {

	@Autowired
	GithubDataGrabber githubGrabber;
	@Autowired
	SonarCubeDataGrabber sonarCubeGrabber;
	@Autowired
	GithubObjectTranslator githubTranslator;
	@Autowired
	SonarCubeObjectTranslator sonarCubeTranslator;
	@Autowired
	BotController botController;

	/**
	 * This method gets all requests with all comments from an api translated into a
	 * bot object.
	 * 
	 * @param gitConfig
	 * @return botRequests
	 * @throws Exception
	 */
	public BotPullRequests getRequestsWithComments(GitConfiguration gitConfig) throws Exception {
		// Init bot object
		BotPullRequests botRequests = null;

		// Pick correct filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
			// Get data from github
			GithubPullRequests githubRequests = githubGrabber.getAllPullRequests(gitConfig);
			// Translate github object
			botRequests = githubTranslator.translateRequests(githubRequests, gitConfig);
			// Check if max amount of requests reached
			botController.checkAmountOfBotRequests(botRequests, gitConfig);
			break;
		}
		return botRequests;
	}

	/**
	 * This method updates a pull request of a specific filehoster.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws Exception
	 * @throws OperationNotSupportedException
	 */
	public void makeUpdateRequest(BotPullRequest request, BotPullRequestComment comment, GitConfiguration gitConfig)
			throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
			// Create updateRequest
			GithubUpdateRequest updateRequest = githubTranslator.makeUpdateRequest(request, gitConfig);
			// Update Request
			githubGrabber.updatePullRequest(updateRequest, gitConfig, request.getRequestNumber());
			// Reply to comment
			githubGrabber.responseToBotComment(githubTranslator.createReplyComment(comment, gitConfig, null), gitConfig,
					request.getRequestNumber());
			break;
		}
	}

	/**
	 * This method creates a pull request of a filehoster.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws Exception
	 */
	public void makeCreateRequest(BotPullRequest request, BotPullRequestComment comment, GitConfiguration gitConfig,
			String botBranchName) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
			// Create createRequest
			GithubCreateRequest createRequest = githubTranslator.makeCreateRequest(request, gitConfig, botBranchName);
			// Create request
			GithubPullRequest newGithubRequest = githubGrabber.createRequest(createRequest, gitConfig);
			// Reply to comment
			githubGrabber.responseToBotComment(
					githubTranslator.createReplyComment(comment, gitConfig, newGithubRequest.getHtmlUrl()), gitConfig,
					request.getRequestNumber());
			break;
		}
	}

	/**
	 * This method checks the user input and creates a git configuration.
	 * 
	 * @param repoName
	 * @param repoOwner
	 * @param repoService
	 * @param botToken
	 * @param sonarCubeProjectKey
	 * @param maxAmountRequests
	 * @param projectRootFolder
	 * @return gitConfig
	 * @throws Exception
	 */
	public GitConfiguration createConfigurationForRepo(String repoName, String repoOwner, String repoService,
			String botUsername, String botPassword, String botToken, String analysisService,
			String analysisServiceProjectKey, Integer maxAmountRequests, String projectRootFolder) throws Exception {

		// Init object
		GitConfiguration gitConfig = null;

		// Pick filehoster
		switch (repoService.toLowerCase()) {
		case "github":
			// Check repository
			githubGrabber.checkRepository(repoName, repoOwner);

			// Check bot user and bot token
			githubGrabber.checkGithubUser(botUsername, botToken);

			// Create git configuration and a fork
			gitConfig = githubTranslator.createConfiguration(repoName, repoOwner, botUsername, botPassword, botToken,
					repoService, analysisService, analysisServiceProjectKey, maxAmountRequests, projectRootFolder);
			githubGrabber.createFork(gitConfig);
			return gitConfig;
		default:
			throw new Exception("Filehoster " + "'" + repoService + "' is not supported!");
		}
	}

	/**
	 * This method deletes a repository of a filehoster.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 * @throws OperationNotSupportedException
	 */
	public void deleteRepository(GitConfiguration gitConfig) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
			// Delete repository
			githubGrabber.deleteRepository(gitConfig);
			break;
		}
	}

	/**
	 * This method creates a fork of a repository of a filehoster.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 */
	public void createFork(GitConfiguration gitConfig) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
			// Create fork
			githubGrabber.createFork(gitConfig);
			break;
		}
	}

	/**
	 * This method gets all issues of a Project from a analysis service.
	 * 
	 * @param gitConfig
	 * @return botIssues
	 * @throws Exception
	 */
	public List<BotIssue> getAnalysisServiceIssues(GitConfiguration gitConfig) throws Exception {
		// Pick service
		switch (gitConfig.getAnalysisService()) {
		case "sonarcube":
			// Get issues and translate them
			SonarCubeIssues issues = sonarCubeGrabber.getIssues(gitConfig.getAnalysisServiceProjectKey());
			List<BotIssue> botIssues = sonarCubeTranslator.translateSonarIssue(issues, gitConfig);
			return botIssues;
		default:
			return null;
		}
	}

	/**
	 * This method creates a request on a filehoster if the refactoring was
	 * performed with issues from a analysis tool.
	 * 
	 * @param request
	 * @param gitConfig
	 * @param newBranch
	 * @throws Exception
	 */
	public void makeCreateRequestWithAnalysisService(BotIssue issue, GitConfiguration gitConfig, String newBranch)
			throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
			// Create createRequest
			GithubCreateRequest createRequest = githubTranslator.makeCreateRequestWithAnalysisService(issue, gitConfig,
					newBranch);
			// Create request on filehoster
			githubGrabber.createRequest(createRequest, gitConfig);
			break;
		}
	}
}
