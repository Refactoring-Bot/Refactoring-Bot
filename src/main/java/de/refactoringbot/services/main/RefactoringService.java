package de.refactoringbot.services.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.transaction.NotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import de.refactoringbot.api.main.ApiGrabber;
import de.refactoringbot.api.sonarqube.SonarQubeDataGrabber;
import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.ConfigurationRepository;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.exceptions.GitWorkflowException;
import de.refactoringbot.model.exceptions.ReviewCommentUnclearException;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.refactoredissue.RefactoredIssue;
import de.refactoringbot.model.refactoredissue.RefactoredIssueRepository;
import de.refactoringbot.refactoring.RefactoringOperations;
import de.refactoringbot.refactoring.RefactoringPicker;
import de.refactoringbot.services.sonarqube.SonarQubeObjectTranslator;
import de.refactoringbot.services.wit.WitService;
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
	GrammarService grammarService;
	@Autowired
	RefactoringPicker refactoring;
	@Autowired
	SonarQubeObjectTranslator sonarTranslator;
	@Autowired
	BotService botService;
	@Autowired
	WitService witService;

	private static final Logger logger = LoggerFactory.getLogger(RefactoringService.class);

	/**
	 * This method performs a refactoring from a comment or an analysis service
	 * issue.
	 * 
	 * @param configID
	 * @param isCommentRefactoring
	 * @return response
	 * @throws Exception
	 */
	public ResponseEntity<?> performRefactoring(Long configID, boolean isCommentRefactoring) throws Exception {

		// Check and create configuration
		GitConfiguration config = checkConfigurationExistance(configID);

		// If analysis service data is missing
		if (!isCommentRefactoring
				&& (config.getAnalysisService() == null || config.getAnalysisServiceProjectKey() == null)) {
			throw new BotRefactoringException("Configuration is missing analysis service data!");
		}

		// Get all pull requests
		BotPullRequests allRequests = getPullRequests(config);
		// Count all open pull requests created by bot
		int amountOfBotRequests = botService.getAmountOfBotRequests(allRequests, config);

		// Return all refactored issues
		if (isCommentRefactoring) {
			return processComments(config, allRequests, amountOfBotRequests);
		} else {
			return processAnalysisIssues(config, amountOfBotRequests);
		}
	}

	/**
	 * This method processes the refactoring of issues detected by an analysis
	 * service.
	 * 
	 * @param config
	 * @param allIssues
	 * @return response
	 */
	private ResponseEntity<?> processAnalysisIssues(GitConfiguration config, int amountBotRequests) {
		List<RefactoredIssue> allRefactoredIssues = new ArrayList<>();
		try {
			// Get issues from analysis service API
			List<BotIssue> botIssues = getBotIssues(config);

			// Iterate all issues
			for (BotIssue botIssue : botIssues) {
				// When Bot-Pull-Request-Limit reached -> return
				if (amountBotRequests >= config.getMaxAmountRequests()) {
					// Return all refactored issues
					return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
				}

				try {
					// If issue was not already refactored
					if (isAnalysisIssueValid(botIssue)) {
						// Perform refactoring
						allRefactoredIssues.add(refactorIssue(false, false, config, null, null, botIssue));
						amountBotRequests++;
					}
				} catch (Exception e) {
					// Create failed Refactored-Object
					botIssue.setErrorMessage("Bot could not refactor this issue! Internal server error!");
					allRefactoredIssues.add(processFailedRefactoring(config, null, null, botIssue, false));
					logger.error(e.getMessage(), e);
				}
			}

			return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * This method processes the comment driven refactoring.
	 * 
	 * @param config
	 * @param allRequests
	 * @param amountOfBotRequests
	 * @return response
	 */
	private ResponseEntity<?> processComments(GitConfiguration config, BotPullRequests allRequests,
			int amountBotRequests) {
		List<RefactoredIssue> allRefactoredIssues = new ArrayList<>();

		for (BotPullRequest request : allRequests.getAllPullRequests()) {
			for (BotPullRequestComment comment : request.getAllComments()) {
				if (isAlreadyRefactored(config, comment)) {
					continue;
				}

				BotIssue botIssue;

				if (grammarService.isBotMentionedInComment(comment.getCommentBody(), config)
						&& !grammarService.isCommentByBot(comment.getUsername(), config)) {
					// If can NOT parse comment with ANTLR
					if (!grammarService.checkComment(comment.getCommentBody(), config)) {
						// Try to parse with wit.ai
						try {
							botIssue = witService.createBotIssue(config, comment);
							logger.info("Comment translated with 'wit.ai': " + comment.getCommentBody());
						} catch (IOException e) {
							logger.error(e.getMessage(), e);
							botIssue = createBotIssueFromInvalidComment(comment, e.getMessage());
							allRefactoredIssues.add(processFailedRefactoring(config, comment, request, botIssue, true));
							return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
						} catch (ReviewCommentUnclearException e) {
							logger.warn(
									"Comment translation with 'wit.ai' failed! Comment: " + comment.getCommentBody());
							botIssue = createBotIssueFromInvalidComment(comment, e.getMessage());
							allRefactoredIssues.add(processFailedRefactoring(config, comment, request, botIssue, true));
							continue;
						}
					} else {
						// Try to refactor with ANTRL4
						try {
							// If ANTLR can parse -> create Issue
							botIssue = grammarService.createIssueFromComment(comment, config);
							logger.info("Comment translated with 'ANTLR': " + comment.getCommentBody());
						} catch (Exception g) {
							logger.error(g.getMessage(), g);
							// If refactoring failed
							botIssue = createBotIssueFromInvalidComment(comment, g.getMessage());
							allRefactoredIssues.add(processFailedRefactoring(config, comment, request, botIssue, true));
							continue;
						}
					}
					// Refactor the created BotIssue
					allRefactoredIssues.add(refactorComment(config, botIssue, request, comment, amountBotRequests));
				}
			}
		}

		return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
	}

	/**
	 * This method refactors the BotIssue which was created from a comment. It
	 * returns a RefactoredIssue after a successful or failed refactoring.
	 * 
	 * @param config
	 * @param issue
	 * @param request
	 * @param comment
	 * @param amountBotRequests
	 * @return refactoredIssue
	 */
	private RefactoredIssue refactorComment(GitConfiguration config, BotIssue botIssue, BotPullRequest request,
			BotPullRequestComment comment, int amountBotRequests) {

		RefactoredIssue refactoredIssue = null;

		// Refactor the created BotIssue
		try {
			// For Requests created by someone else
			if (!request.getCreatorName().equals(config.getBotName())) {
				if (amountBotRequests >= config.getMaxAmountRequests()) {
					return null;
				}
				// Perform refactoring
				refactoredIssue = refactorIssue(false, true, config, comment, request, botIssue);
				amountBotRequests++;
				// For Requests created by the bot
			} else {
				refactoredIssue = refactorIssue(true, true, config, comment, request, botIssue);
			}
		} catch (BotRefactoringException e) {
			// If refactoring failed
			botIssue.setErrorMessage(e.getMessage());
			refactoredIssue = processFailedRefactoring(config, comment, request, botIssue, true);
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			// If botservice failed before or after the refactoring
			botIssue.setErrorMessage("Bot could not refactor this comment! Internal server error!");
			refactoredIssue = processFailedRefactoring(config, comment, request, botIssue, true);
			logger.error(e.getMessage(), e);
		}

		return refactoredIssue;
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
	private RefactoredIssue refactorIssue(boolean isBotPR, boolean isCommentRefactoring, GitConfiguration config,
			BotPullRequestComment comment, BotPullRequest request, BotIssue botIssue) throws Exception {
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

					// Save and return refactored issue
					return issueRepo.save(refactoredIssue);
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

					// Save and return refactored issue
					return issueRepo.save(refactoredIssue);
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

				// Save and return refactored issue
				return issueRepo.save(refactoredIssue);
			}
		}

		botIssue.setErrorMessage("Could not create a commit message!");
		return processFailedRefactoring(config, comment, request, botIssue, isCommentRefactoring);
	}

	/**
	 * This method checks if the database contains a configuration with given id and
	 * returns it if it exists.
	 * 
	 * @param configurationId
	 * @return savedConfig
	 * @throws DatabaseConnectionException
	 * @throws NotFoundException
	 */
	private GitConfiguration checkConfigurationExistance(Long configurationId)
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

	/**
	 * This method returns all pull requests from a filehosting service.
	 * 
	 * @param config
	 * @return allRequests
	 * @throws Exception
	 */
	public BotPullRequests getPullRequests(GitConfiguration config)
			throws URISyntaxException, GitHubAPIException, IOException, GitWorkflowException {
		// Fetch target-Repository-Data
		dataGetter.fetchRemote(config);

		return grabber.getRequestsWithComments(config);
	}

	/**
	 * This method checks if a comment was already refactored in the past.
	 * 
	 * @param config
	 * @param comment
	 * @return isAlreadyRefactored
	 */
	public boolean isAlreadyRefactored(GitConfiguration config, BotPullRequestComment comment) {
		return issueRepo.refactoredComment(config.getRepoService(), comment.getCommentID().toString()).isPresent();
	}

	/**
	 * This method checks if a from a AnalysisServiceIssue translated BotIssue is
	 * valid, e.g. not already refactored.
	 * 
	 * @param config
	 * @param issue
	 * @return
	 */
	public boolean isAnalysisIssueValid(BotIssue issue) {
		return (!issueRepo.refactoredAnalysisIssue(issue.getCommentServiceID()).isPresent());
	}

	/**
	 * This method processes a faild refactoring. It creates a RefactoredIssue
	 * object and saves it to the database so that the bot won't try to refactor a
	 * comment that can not be refactored. Also, a reply is sent to the comment
	 * creator to inform him of the failure with a proper error message.
	 * 
	 * @param config
	 * @param comment
	 * @param request
	 * @param botIssue
	 * @return failedIssue
	 */
	private RefactoredIssue processFailedRefactoring(GitConfiguration config, BotPullRequestComment comment,
			BotPullRequest request, BotIssue botIssue, boolean isCommentRefactoring) {
		// Create failedIssue
		RefactoredIssue failedIssue = botController.buildRefactoredIssue(botIssue, config);

		// Reply to user if refactoring comments
		if (isCommentRefactoring) {
			try {
				grabber.replyToUserForFailedRefactoring(request, comment, config,
						createErrorMessage(botIssue.getErrorMessage()));
			} catch (Exception u) {
				logger.error(u.getMessage(), u);
			}
		}

		// Save failed refactoring and return it
		return issueRepo.save(failedIssue);
	}

	/**
	 * This method creates a reply message with the help from the error message of a
	 * BotRefactoringException.
	 * 
	 * @param exceptionMessage
	 * @return replyMessage
	 */
	private String createErrorMessage(String exceptionMessage) {
		return exceptionMessage + " Please fix this issue by rephrasing your instruction in a new comment.";
	}

	/**
	 * This method creates an BotIssue for a failed refactoring because of a invalid
	 * comment that the bot can not understand.
	 * 
	 * @param comment
	 * @param errorMessage
	 * @return issue
	 */
	private BotIssue createBotIssueFromInvalidComment(BotPullRequestComment comment, String errorMessage) {
		// Create object
		BotIssue issue = new BotIssue();

		// Add data to comment
		issue.setCommentServiceID(comment.getCommentID().toString());
		issue.setLine(comment.getPosition());
		issue.setFilePath(comment.getFilepath());
		issue.setErrorMessage(errorMessage);
		issue.setRefactoringOperation(RefactoringOperations.UNKNOWN);

		return issue;
	}

	/**
	 * This method collects all issues from a analysis service and translates them
	 * to BotIssues.
	 * 
	 * @param config
	 * @return botIssues
	 * @throws Exception
	 */
	private List<BotIssue> getBotIssues(GitConfiguration config) throws Exception {
		List<BotIssue> botIssues = grabber.getAnalysisServiceIssues(config);

		// BotIssues are null if analysis service not supported
		if (botIssues == null) {
			throw new NotSupportedException("Analysis-Service '" + config.getAnalysisService() + "' is not supported!");
		}

		return botIssues;
	}
}
