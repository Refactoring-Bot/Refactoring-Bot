package de.refactoringBot.api.main;

import java.util.List;

import javax.naming.OperationNotSupportedException;

import de.refactoringBot.model.configuration.GitConfigurationDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringBot.api.github.GithubDataGrabber;
import de.refactoringBot.api.sonarQube.SonarQubeDataGrabber;
import de.refactoringBot.controller.github.GithubObjectTranslator;
import de.refactoringBot.controller.main.BotController;
import de.refactoringBot.controller.sonarQube.SonarQubeObjectTranslator;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.githubModels.pullRequest.GithubCreateRequest;
import de.refactoringBot.model.githubModels.pullRequest.GithubPullRequest;
import de.refactoringBot.model.githubModels.pullRequest.GithubPullRequests;
import de.refactoringBot.model.outputModel.botPullRequest.BotPullRequest;
import de.refactoringBot.model.outputModel.botPullRequest.BotPullRequests;
import de.refactoringBot.model.outputModel.botPullRequestComment.BotPullRequestComment;
import de.refactoringBot.model.sonarQube.SonarQubeIssues;

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
	SonarQubeDataGrabber sonarQubeGrabber;
	@Autowired
	GithubObjectTranslator githubTranslator;
	@Autowired
	SonarQubeObjectTranslator sonarQubeTranslator;
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
			break;
		}
		return botRequests;
	}

	/**
	 * This method replies to User inside a Pull-Request that belongs to a Bot if
	 * the refactoring was successful.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws Exception
	 * @throws OperationNotSupportedException
	 */
	public void replyToUserInsideBotRequest(BotPullRequest request, BotPullRequestComment comment,
			GitConfiguration gitConfig) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
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
	 * Reply to comment if refactoring failed.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws Exception
	 */
	public void replyToUserForFailedRefactoring(BotPullRequest request, BotPullRequestComment comment,
			GitConfiguration gitConfig, String errorMessage) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
			// Reply to comment
			githubGrabber.responseToBotComment(githubTranslator.createFailureReply(comment, errorMessage), gitConfig,
					request.getRequestNumber());
			break;
		}
	}

	/**
	 * Check if Branch exists on repository.
	 * 
	 * @param request
	 * @param gitConfig
	 * @throws Exception
	 */
	public void checkBranch(GitConfiguration gitConfig, String branchName) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case "github":
			// Reply to comment
			githubGrabber.checkBranch(gitConfig, branchName);
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
	public GitConfiguration createConfigurationForRepo(GitConfigurationDTO configuration) throws Exception {

		// Init object
		GitConfiguration gitConfig = null;

		// Check analysis service data
		checkAnalysisService(configuration.getAnalysisService(), configuration.getAnalysisServiceProjectKey());

		// Pick filehoster
		switch (configuration.getRepoService().toLowerCase()) {
		case "github":
			// Check repository
			githubGrabber.checkRepository(configuration.getRepoName(), configuration.getRepoOwner());

			// Check bot user and bot token
			githubGrabber.checkGithubUser(configuration.getBotName(), configuration.getBotToken(),
					configuration.getBotEmail());

			// Create git configuration and a fork
			gitConfig = githubTranslator.createConfiguration(configuration);
			return gitConfig;
		default:
			throw new Exception("Filehoster " + "'" + configuration.getRepoService() + "' is not supported!");
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
			// Create fork if not already exists
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
		case "sonarqube":
			// Get issues and translate them
			SonarQubeIssues issues = sonarQubeGrabber.getIssues(gitConfig.getAnalysisServiceProjectKey());
			List<BotIssue> botIssues = sonarQubeTranslator.translateSonarIssue(issues, gitConfig);
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

	/**
	 * This method checks the analysis service data.
	 * 
	 * @param analysisService
	 * @param analysisServiceProjectKey
	 */
	private void checkAnalysisService(String analysisService, String analysisServiceProjectKey) throws Exception {
		// Check if input exists
		if (analysisService == null || analysisServiceProjectKey == null) {
			return;
		}
		// Pick service
		switch (analysisService.toLowerCase()) {
		case "sonarqube":
			sonarQubeGrabber.checkSonarData(analysisServiceProjectKey);
			break;
		}

	}
}
