package de.refactoringbot.api.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.OperationNotSupportedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringbot.api.github.GithubDataGrabber;
import de.refactoringbot.api.gitlab.GitlabDataGrabber;
import de.refactoringbot.api.sonarqube.SonarQubeDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.github.pullrequest.GithubCreateRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequests;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.sonarqube.SonarQubeIssues;
import de.refactoringbot.services.github.GithubObjectTranslator;
import de.refactoringbot.services.gitlab.GitlabObjectTranslator;
import de.refactoringbot.services.main.BotService;
import de.refactoringbot.services.sonarqube.SonarQubeObjectTranslator;

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
	GitlabDataGrabber gitlabGrabber;
	@Autowired
	SonarQubeDataGrabber sonarQubeGrabber;
	@Autowired
	GithubObjectTranslator githubTranslator;
	@Autowired
	GitlabObjectTranslator gitlabTranslator;
	@Autowired
	SonarQubeObjectTranslator sonarQubeTranslator;
	@Autowired
	BotService botController;

	/**
	 * This method gets all requests with all comments from an api translated into a
	 * bot object.
	 * 
	 * @param gitConfig
	 * @return botRequests
	 * @throws URISyntaxException
	 * @throws GitHubAPIException
	 * @throws IOException
	 */
	public BotPullRequests getRequestsWithComments(GitConfiguration gitConfig)
			throws URISyntaxException, GitHubAPIException, IOException {

		BotPullRequests botRequests = null;

		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			GithubPullRequests githubRequests = githubGrabber.getAllPullRequests(gitConfig);
			botRequests = githubTranslator.translateRequests(githubRequests, gitConfig);
			break;
		case gitlab:
			// TODO
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
		case github:
			githubGrabber.responseToBotComment(githubTranslator.createReplyComment(comment, null), gitConfig,
					request.getRequestNumber());
			break;
		case gitlab:
			// TODO
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
		case github:
			// Create PR Object
			GithubCreateRequest createRequest = githubTranslator.makeCreateRequest(request, gitConfig, botBranchName);
			// Create PR on filehoster
			GithubPullRequest newGithubRequest = githubGrabber.createRequest(createRequest, gitConfig);
			githubGrabber.responseToBotComment(
					githubTranslator.createReplyComment(comment, newGithubRequest.getHtmlUrl()), gitConfig,
					request.getRequestNumber());
			break;
		case gitlab:
			// TODO
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
		case github:
			githubGrabber.responseToBotComment(githubTranslator.createFailureReply(comment, errorMessage), gitConfig,
					request.getRequestNumber());
			break;
		case gitlab:
			// TODO
			break;
		}
	}

	/**
	 * Check if Branch exists on repository.
	 * 
	 * @param gitConfig
	 * @throws Exception
	 */
	public void checkBranch(GitConfiguration gitConfig, String branchName) throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			githubGrabber.checkBranch(gitConfig, branchName);
			break;
		case gitlab:
			// TODO
			break;
		}
	}

	/**
	 * This method checks the user input and creates a git configuration.
	 * 
	 * @param configuration
	 * @return gitConfig
	 * @throws Exception
	 */
	public GitConfiguration createConfigurationForRepo(GitConfigurationDTO configuration) throws Exception {
		GitConfiguration gitConfig = null;
		// Check analysis service data
		checkAnalysisService(configuration);

		// Pick filehoster
		switch (configuration.getRepoService()) {
		case github:
			githubGrabber.checkRepository(configuration.getRepoName(), configuration.getRepoOwner(),
					configuration.getBotToken());
			githubGrabber.checkGithubUser(configuration.getBotName(), configuration.getBotToken(),
					configuration.getBotEmail());

			// Create git configuration and a fork
			gitConfig = githubTranslator.createConfiguration(configuration);
			return gitConfig;
		case gitlab:
			gitlabGrabber.checkRepository(configuration.getRepoName(), configuration.getRepoOwner(),
					configuration.getBotToken());
			gitlabGrabber.checkGithubUser(configuration.getBotName(), configuration.getBotToken(),
					configuration.getBotEmail());

			// Create git configuration and a fork
			gitConfig = gitlabTranslator.createConfiguration(configuration);
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
		case github:
			githubGrabber.deleteRepository(gitConfig);
			break;
		case gitlab:
			gitlabGrabber.deleteRepository(gitConfig);
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
		case github:
			githubGrabber.createFork(gitConfig);
			break;
		case gitlab:
			gitlabGrabber.createFork(gitConfig);
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
		case sonarqube:
			// Get issues and translate them
			List<SonarQubeIssues> issues = sonarQubeGrabber.getIssues(gitConfig);
			List<BotIssue> botIssues = new ArrayList<>();
			for (SonarQubeIssues i : issues) {
				botIssues.addAll(sonarQubeTranslator.translateSonarIssue(i, gitConfig));
			}
			return botIssues;
		default:
			return null;
		}
	}

	/**
	 * This method creates a request on a filehoster if the refactoring was
	 * performed with issues from a analysis tool.
	 *
	 * @param issue
	 * @param gitConfig
	 * @param newBranch
	 * @throws Exception
	 */
	public void makeCreateRequestWithAnalysisService(BotIssue issue, GitConfiguration gitConfig, String newBranch)
			throws Exception {
		// Pick filehoster
		switch (gitConfig.getRepoService()) {
		case github:
			// Create PR object
			GithubCreateRequest createRequest = githubTranslator.makeCreateRequestWithAnalysisService(issue, gitConfig,
					newBranch);
			// Create PR on filehoster
			githubGrabber.createRequest(createRequest, gitConfig);
			break;
		case gitlab:
			// TODO
			break;
		}
	}

	/**
	 * This method checks the analysis service data.
	 * 
	 * @param analysisService
	 * @param analysisServiceProjectKey
	 */
	private void checkAnalysisService(GitConfigurationDTO configuration) throws Exception {
		// Check if input exists
		if (configuration.getAnalysisService() == null || configuration.getAnalysisServiceProjectKey() == null
				|| configuration.getAnalysisServiceApiLink() == null) {
			return;
		}
		// Pick service
		switch (configuration.getAnalysisService()) {
		case sonarqube:
			sonarQubeGrabber.checkSonarData(configuration);
			break;
		}

	}
}
