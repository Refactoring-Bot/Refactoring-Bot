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
import de.refactoringbot.model.exceptions.CommentUnderstandingMessage;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.exceptions.GitWorkflowException;
import de.refactoringbot.model.exceptions.WitAPIException;
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
	public ResponseEntity<?> processAnalysisIssues(GitConfiguration config, int amountBotRequests) {
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
						allRefactoredIssues = refactorIssue(false, false, config, null, null, botIssue,
								allRefactoredIssues);
						amountBotRequests++;
					}
				} catch (Exception e) {
					// Create failed Refactored-Object
					botIssue.setErrorMessage("Bot could not refactor this comment! Internal server error!");
					allRefactoredIssues = processFailedRefactoring(allRefactoredIssues, config, null, null, botIssue,
							false);
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
	public ResponseEntity<?> processComments(GitConfiguration config, BotPullRequests allRequests,
			int amountBotRequests) {
		List<RefactoredIssue> allRefactoredIssues = new ArrayList<>();
		// Iterate through all requests
		for (BotPullRequest request : allRequests.getAllPullRequests()) {
			// Iterate through all comments
			for (BotPullRequestComment comment : request.getAllComments()) {
				// When Bot-Pull-Request-Limit reached -> return
				if (amountBotRequests >= config.getMaxAmountRequests()) {
					// Return all refactored issues
					return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
				}

				// If comment was already refactored in past
				if (isAlreadyRefactored(config, comment)) {
					// Continue with next comment.
					continue;
				}

				// Create issue
				BotIssue botIssue;

				// Check if comment is meant for the bot
				if (witService.isBotComment(comment, config)) {
					try {
						botIssue = witService.createBotIssue(config, comment);
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
						botIssue = returnInvalidCommentIssue(config, comment, request, e.getMessage());
						allRefactoredIssues = processFailedRefactoring(allRefactoredIssues, config, comment, request,
								botIssue, true);
						return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
					} catch (CommentUnderstandingMessage | WitAPIException e) {
						logger.warn("Comment translation with 'wit.ai' failed! Comment: " + comment.getCommentBody());
						// Try to parse comment with ANTLR
						if (!grammarService.checkComment(comment.getCommentBody())) {
							botIssue = returnInvalidCommentIssue(config, comment, request, e.getMessage());
							allRefactoredIssues = processFailedRefactoring(allRefactoredIssues, config, comment, request,
									botIssue, true);
							continue;
						}
						// Try to refactor with ANTRL4
						try {
							// If ANTLR can parse -> create Issue
							botIssue = createIssueFromComment(config, comment);
						} catch (Exception g) {
							logger.error(g.getMessage(), g);
							// If refactoring failed
							botIssue = returnInvalidCommentIssue(config, comment, request, g.getMessage());
							allRefactoredIssues = processFailedRefactoring(allRefactoredIssues, config, comment, request,
									botIssue, true);
							continue;
						}
					}

					// Check if comment is valid and not already refactored

					try {
						// For Requests created by someone else
						if (!request.getCreatorName().equals(config.getBotName())) {
							// Perform refactoring
							allRefactoredIssues = refactorIssue(false, true, config, comment, request, botIssue,
									allRefactoredIssues);
							amountBotRequests++;
							// For Requests created by the bot
						} else {
							allRefactoredIssues = refactorIssue(true, true, config, comment, request, botIssue,
									allRefactoredIssues);
						}
					} catch (BotRefactoringException e) {
						// If refactoring failed
						botIssue.setErrorMessage(e.getMessage());
						allRefactoredIssues = processFailedRefactoring(allRefactoredIssues, config, comment, request,
								botIssue, true);
						logger.error(e.getMessage(), e);
						// Catch other errors
					} catch (Exception e) {
						// If botservice faild before or after the refactoring
						botIssue.setErrorMessage("Bot could not refactor this comment! Internal server error!");
						allRefactoredIssues = processFailedRefactoring(allRefactoredIssues, config, comment, request,
								botIssue, true);
						logger.error(e.getMessage(), e);
					}
				}
			}
		}

		return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
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

	/**
	 * This method returns all pull requests from a filehosting service.
	 * 
	 * @param config
	 * @return allRequests
	 * @throws Exception
	 */
	public BotPullRequests getPullRequests(GitConfiguration config)
			throws URISyntaxException, GitHubAPIException, IOException, BotRefactoringException, GitWorkflowException {
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
	 * This method creates an BotIssue for refactoring from a PullRequest-Comment.
	 * 
	 * @param config
	 * @param comment
	 * @return botIssue
	 * @throws Exception
	 */
	public BotIssue createIssueFromComment(GitConfiguration config, BotPullRequestComment comment) throws Exception {
		return grammarService.createIssueFromComment(comment, config);
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
	 * This method creates an BotIssue for a failed refactoring because of a invalid
	 * comment that the bot can not understand.
	 * 
	 * @param config
	 * @param comment
	 * @param request
	 * @param errorMessage
	 * @return issue
	 */
	public BotIssue returnInvalidCommentIssue(GitConfiguration config, BotPullRequestComment comment,
			BotPullRequest request, String errorMessage) {
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
