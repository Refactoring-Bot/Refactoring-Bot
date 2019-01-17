package de.refactoringbot.services.main;

import java.util.List;
import java.util.Optional;

import javax.transaction.NotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.api.main.ApiGrabber;
import de.refactoringbot.api.sonarqube.SonarQubeDataGrabber;
import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.ConfigurationRepository;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.refactoredissue.RefactoredIssue;
import de.refactoringbot.model.refactoredissue.RefactoredIssueRepository;
import de.refactoringbot.refactoring.RefactoringPicker;
import de.refactoringbot.services.sonarqube.SonarQubeObjectTranslator;
import javassist.NotFoundException;

/**
 * This class contains functions regarding the automated refactorings.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class RefactoringService {

	@Autowired
	ApiGrabber grabber;
	@Autowired
	SonarQubeDataGrabber sonarCubeGrabber;
	@Autowired
	GitService dataGetter;
	@Autowired
	ConfigurationRepository configRepo;
	@Autowired
	RefactoredIssueRepository issueRepo;
	@Autowired
	BotConfiguration botConfig;
	@Autowired
	BotService botController;
	@Autowired
	GrammarService grammarController;
	@Autowired
	RefactoringPicker refactoring;
	@Autowired
	SonarQubeObjectTranslator sonarTranslator;

	private static final Logger logger = LoggerFactory.getLogger(RefactoringService.class);

	/**
	 * This method checks if the database contains a configuration with given id and
	 * returns it if it exists.
	 * 
	 * @param configurationId
	 * @return savedConfig
	 * @throws DatabaseConnectionException
	 * @throws NotFoundException
	 */
	public GitConfiguration checkConfigurationExistance(Long configurationId)
			throws DatabaseConnectionException, NotFoundException {

		Optional<GitConfiguration> gitConfig;
		GitConfiguration savedConfig = null;

		try {
			// Try to get the Git-Configuration with the given ID
			gitConfig = configRepo.getByID(configurationId);
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			throw new DatabaseConnectionException("Connection with database failed!");
		}
		// If Configuration does not exist
		if (gitConfig.isPresent()) {
			savedConfig = gitConfig.get();
		} else {
			throw new NotFoundException("Configuration with given ID does not exist in the database!");
		}

		return savedConfig;
	}

	public BotPullRequests getPullRequests(GitConfiguration config) throws Exception {
		// Fetch target-Repository-Data
		dataGetter.fetchRemote(config);

		// Get Pull-Requests with comments
		BotPullRequests allRequests = grabber.getRequestsWithComments(config);
		Integer amountBotRequests = botController.getAmountOfBotRequests(allRequests, config);

		// Check if max amount is reached
		if (amountBotRequests >= config.getMaxAmountRequests()) {
			throw new BotRefactoringException("Maximal amount of requests reached." + "(Maximum = "
					+ config.getMaxAmountRequests() + "; Currently = " + amountBotRequests + " bot requests are open)");
		}

		return allRequests;
	}

	/**
	 * This method checks if a comment is valid for refactoring. The service will
	 * only refactor the comment if it was not already refactored in the past and if
	 * the comment fulfills the bot grammar.
	 * 
	 * @param config
	 * @param comment
	 * @return isValid
	 */
	public boolean isCommentValid(GitConfiguration config, BotPullRequestComment comment) {
		if (grammarController.checkComment(comment.getCommentBody()) && !issueRepo
				.refactoredComment(config.getRepoService(), comment.getCommentID().toString()).isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method checks if a from a AnalysisServiceIssue translated BotIssue is
	 * valid, e.g. not already refactored.
	 * 
	 * @param config
	 * @param issue
	 * @return
	 */
	public boolean isAnalysisIssueValid(GitConfiguration config, BotIssue issue) {
		if (!issueRepo.refactoredAnalysisIssue(issue.getCommentServiceID()).isPresent()) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * This method creates an BotIssue for refactoring from a PullRequest-Comment.
	 * 
	 * @param config
	 * @param comment
	 * @return botIssue
	 * @throws Exception
	 */
	public BotIssue createIssueFromComment(GitConfiguration config, BotPullRequestComment comment) throws Exception {
		BotIssue botIssue = grammarController.createIssueFromComment(comment, config);
		return botIssue;
	}

	/**
	 * This method configures the local workspace, refactors the issue, pushes the
	 * changes and creates an PR.
	 * 
	 * @param isBotPR
	 * @param isCommentRefactoring
	 * @param config
	 * @param comment
	 * @param request
	 * @param botIssue
	 * @param allRefactoredIssues
	 * @return allRefactoredIssues
	 * @throws Exception
	 */
	public List<RefactoredIssue> refactorIssue(boolean isBotPR, boolean isCommentRefactoring, GitConfiguration config,
			BotPullRequestComment comment, BotPullRequest request, BotIssue botIssue,
			List<RefactoredIssue> allRefactoredIssues) throws Exception {
		// If refactoring via comment
		if (isCommentRefactoring) {
			// If PR owner = bot
			if (isBotPR) {
				// Change to existing Refactoring-Branch
				dataGetter.switchBranch(config, request.getBranchName());

				// Try to refactor
				botIssue.setCommitMessage(refactoring.pickAndRefactor(botIssue, config));

				// If successful
				if (botIssue.getCommitMessage() != null) {
					// Create Refactored-Object
					RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, config);

					// Push changes
					dataGetter.pushChanges(config, botIssue.getCommitMessage());
					// Reply to User
					grabber.replyToUserInsideBotRequest(request, comment, config);

					// Save to Database + add to list
					RefactoredIssue savedIssue = issueRepo.save(refactoredIssue);
					allRefactoredIssues.add(savedIssue);
				}
				// If PR owner != bot
			} else {
				// Create refactoring branch with Filehoster-Service + Comment-ID
				String newBranch = config.getRepoService() + "_Refactoring_" + comment.getCommentID().toString();
				// Check if branch already exists (throws exception if it does)
				grabber.checkBranch(config, newBranch);
				// Create new Branch
				dataGetter.createBranch(config, request.getBranchName(), newBranch, "upstream");
				// Try to refactor
				botIssue.setCommitMessage(refactoring.pickAndRefactor(botIssue, config));

				// If successful
				if (botIssue.getCommitMessage() != null) {
					// Create Refactored-Object
					RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, config);

					// Push changes + create Pull-Request
					dataGetter.pushChanges(config, botIssue.getCommitMessage());
					grabber.makeCreateRequest(request, comment, config, newBranch);

					// Save to Database + add to list
					RefactoredIssue savedIssue = issueRepo.save(refactoredIssue);
					allRefactoredIssues.add(savedIssue);
				}
			}
			// If analysis service refactoring
		} else {
			// Create new branch for refactoring
			String newBranch = "sonarCube_Refactoring_" + botIssue.getCommentServiceID();
			// Check if branch already exists (throws exception if it does)
			grabber.checkBranch(config, newBranch);
			dataGetter.createBranch(config, "master", newBranch, "upstream");
			// Try to refactor
			botIssue.setCommitMessage(refactoring.pickAndRefactor(botIssue, config));

			// If successful
			if (botIssue.getCommitMessage() != null) {
				// Create Refactored-Object
				RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, config);

				// Push changes + create Pull-Request
				dataGetter.pushChanges(config, botIssue.getCommitMessage());
				grabber.makeCreateRequestWithAnalysisService(botIssue, config, newBranch);

				// Save to database + add to list
				RefactoredIssue savedIssue = issueRepo.save(refactoredIssue);
				allRefactoredIssues.add(savedIssue);
			}
		}

		return allRefactoredIssues;
	}

	/**
	 * This method processes a faild refactoring. It creates a RefactoredIssue
	 * object and saves it to the database so that the bot won't try to refactor a
	 * comment that can not be refactored. Also, a reply is sent to the comment
	 * creator to inform him of the failure with a proper error message.
	 * 
	 * @param allRefactoredIssues
	 * @param config
	 * @param comment
	 * @param request
	 * @param botIssue
	 * @return allRefactoredIssues
	 */
	public List<RefactoredIssue> processFailedRefactoring(List<RefactoredIssue> allRefactoredIssues,
			GitConfiguration config, BotPullRequestComment comment, BotPullRequest request, BotIssue botIssue,
			boolean isCommentRefactoring) {
		// Create failedIssue
		RefactoredIssue failedIssue = botController.buildRefactoredIssue(botIssue, config);

		// Reply to user if refactoring comments
		if (isCommentRefactoring) {
			try {
				grabber.replyToUserForFailedRefactoring(request, comment, config, botIssue.getErrorMessage());
			} catch (Exception u) {
				logger.error(u.getMessage(), u);
			}
		}

		// Save failed refactoring to database + add to list
		RefactoredIssue savedFailIssue = issueRepo.save(failedIssue);
		allRefactoredIssues.add(savedFailIssue);

		return allRefactoredIssues;
	}

	/**
	 * This method collects all issues from a analysis service and translates them
	 * to BotIssues.
	 * 
	 * @param config
	 * @return botIssues
	 * @throws Exception
	 */
	public List<BotIssue> getBotIssues(GitConfiguration config) throws Exception {
		// Get BotIssues from AnalysisServiceIssues
		List<BotIssue> botIssues = grabber.getAnalysisServiceIssues(config);

		// BotIssues are null if analysis service not supported
		if (botIssues == null) {
			throw new NotSupportedException("Analysis-Service '" + config.getAnalysisService() + "' is not supported!");
		}

		return botIssues;
	}
}
