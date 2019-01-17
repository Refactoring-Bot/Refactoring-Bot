package de.refactoringbot.rest;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.refactoringbot.services.main.BotService;
import de.refactoringbot.services.main.RefactoringService;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.refactoredissue.RefactoredIssue;
import io.swagger.annotations.ApiOperation;
import javassist.NotFoundException;

/**
 * This REST-Controller creates an REST-API which allows the user to perform
 * refactorings with the bot.
 * 
 * @author Stefan Basaric
 *
 */
@RestController
@RequestMapping(path = "/configurations")
public class RefactoringController {

	@Autowired
	RefactoringService refactoringService;
	@Autowired
	BotService botService;

	private static final Logger logger = LoggerFactory.getLogger(RefactoringController.class);

	/**
	 * This method performs refactorings with comments within Pull-Requests of a
	 * Filehoster like GitHub.
	 * 
	 * @param configID
	 * @return allRequests
	 */
	@PostMapping(value = "/{configID}/refactorWithComments", produces = "application/json")
	@ApiOperation(value = "Perform refactorings with Pull-Request-Comments.")
	public ResponseEntity<?> refactorWithComments(@PathVariable Long configID) {

		Integer amountBotRequests = 0;
		List<RefactoredIssue> allRefactoredIssues = new ArrayList<>();
		GitConfiguration config = null;
		BotPullRequests allRequests;

		try {
			config = refactoringService.checkConfigurationExistance(configID);
		} catch (DatabaseConnectionException d) {
			// Print exception and abort if database error occurs
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException n) {
			return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
		}

		try {
			// Get all pull requests
			allRequests = refactoringService.getPullRequests(config);
			// Count all open pull requests created by bot
			amountBotRequests = botService.getAmountOfBotRequests(allRequests, config);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Iterate through all requests
		for (BotPullRequest request : allRequests.getAllPullRequests()) {
			// Iterate through all comments
			for (BotPullRequestComment comment : request.getAllComments()) {
				// When Bot-Pull-Request-Limit reached -> return
				if (amountBotRequests >= config.getMaxAmountRequests()) {
					// Return all refactored issues
					return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
				}

				// Check if comment is valid and not already refactored
				if (refactoringService.isCommentValid(config, comment)) {
					// Create issue
					BotIssue botIssue;
					try {
						botIssue = refactoringService.createIssueFromComment(config, comment);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
					}

					try {
						// For Requests created by someone else
						if (!request.getCreatorName().equals(config.getBotName())) {
							// Perform refactoring
							allRefactoredIssues = refactoringService.refactorIssue(false, true, config, comment,
									request, botIssue, allRefactoredIssues);
							amountBotRequests++;
							// For Requests created by the bot
						} else {
							allRefactoredIssues = refactoringService.refactorIssue(true, true, config, comment, request,
									botIssue, allRefactoredIssues);
						}
					} catch (BotRefactoringException e) {
						// If refactoring failed
						botIssue.setErrorMessage(e.getMessage());
						allRefactoredIssues = refactoringService.processFailedRefactoring(allRefactoredIssues, config,
								comment, request, botIssue, true);
						logger.error(e.getMessage(), e);

						// Catch other errors
					} catch (Exception e) {
						// If botservice faild before or after the refactoring
						botIssue.setErrorMessage("Bot could not refactor this comment! Internal server error!");
						allRefactoredIssues = refactoringService.processFailedRefactoring(allRefactoredIssues, config,
								comment, request, botIssue, true);
						logger.error(e.getMessage(), e);
					}
				}
			}
		}

		// Return all refactored issues
		return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
	}

	/**
	 * This method performs refactorings according to findings with an analysis
	 * service like SonarCube.
	 * 
	 * @param configID
	 * @return allRefactoredIssues
	 */
	@PostMapping(value = "/{configID}/refactorWithAnalysisService", produces = "application/json")
	@ApiOperation(value = "Perform refactorings with analysis service.")
	public ResponseEntity<?> refactorWithSonarCube(@PathVariable Long configID) {

		// Create empty list of refactored Issues
		Integer amountBotRequests = 0;
		List<RefactoredIssue> allRefactoredIssues = new ArrayList<>();
		GitConfiguration config;

		try {
			config = refactoringService.checkConfigurationExistance(configID);
		} catch (DatabaseConnectionException d) {
			// Print exception and abort if database error occurs
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException n) {
			return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
		}

		// If analysis service data is missing
		if (config.getAnalysisService() == null || config.getAnalysisServiceProjectKey() == null) {
			return new ResponseEntity<>("Configuration is missing analysis service data!", HttpStatus.BAD_REQUEST);
		}

		try {
			// Get all pull requests
			BotPullRequests allRequests = refactoringService.getPullRequests(config);
			// Count all open pull requests created by bot
			amountBotRequests = botService.getAmountOfBotRequests(allRequests, config);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		try {
			// Get issues from analysis service API
			List<BotIssue> botIssues = refactoringService.getBotIssues(config);

			// Iterate all issues
			for (BotIssue botIssue : botIssues) {
				// When Bot-Pull-Request-Limit reached -> return
				if (amountBotRequests >= config.getMaxAmountRequests()) {
					// Return all refactored issues
					return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
				}

				try {
					// If issue was not already refactored
					if (refactoringService.isAnalysisIssueValid(config, botIssue)) {
						// Perform refactoring
						allRefactoredIssues = refactoringService.refactorIssue(false, false, config, null,
								null, botIssue, allRefactoredIssues);
						amountBotRequests++;
					}
				} catch (Exception e) {
					// Create failed Refactored-Object
					botIssue.setErrorMessage("Bot could not refactor this comment! Internal server error!");
					allRefactoredIssues = refactoringService.processFailedRefactoring(allRefactoredIssues, config,
							null, null, botIssue, false);
					logger.error(e.getMessage(), e);
				}
			}

			return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
