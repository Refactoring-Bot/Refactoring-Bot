package de.refactoringBot.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import de.refactoringBot.api.main.ApiGrabber;
import de.refactoringBot.api.sonarQube.SonarQubeDataGrabber;
import de.refactoringBot.configuration.BotConfiguration;
import de.refactoringBot.controller.main.BotController;
import de.refactoringBot.controller.main.GitController;
import de.refactoringBot.controller.main.GrammarController;
import de.refactoringBot.controller.sonarQube.SonarQubeObjectTranslator;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.ConfigurationRepository;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.model.outputModel.botPullRequest.BotPullRequest;
import de.refactoringBot.model.outputModel.botPullRequest.BotPullRequests;
import de.refactoringBot.model.outputModel.botPullRequestComment.BotPullRequestComment;
import de.refactoringBot.model.refactoredIssue.RefactoredIssue;
import de.refactoringBot.model.refactoredIssue.RefactoredIssueRepository;
import de.refactoringBot.refactoring.RefactoringPicker;
import io.swagger.annotations.ApiOperation;

/**
 * This REST-Controller creates an REST-API which allows the user to perform
 * refactorings with the bot.
 * 
 * @author Stefan Basaric
 *
 */
@RestController
@RequestMapping(path = "/refactorings")
public class RefactoringController {

	@Autowired
	ApiGrabber grabber;
	@Autowired
	SonarQubeDataGrabber sonarCubeGrabber;
	@Autowired
	GitController dataGetter;
	@Autowired
	ConfigurationRepository configRepo;
	@Autowired
	RefactoredIssueRepository issueRepo;
	@Autowired
	RefactoredIssueRepository refactoredIssues;
	@Autowired
	BotConfiguration botConfig;
	@Autowired
	BotController botController;
	@Autowired
	GrammarController grammarController;
	@Autowired
	RefactoringPicker refactoring;
	@Autowired
	SonarQubeObjectTranslator sonarTranslator;

	/**
	 * This method performs refactorings with comments within Pull-Requests of a
	 * Filehoster like GitHub.
	 * 
	 * @param configID
	 * @return allRequests
	 */
	@RequestMapping(value = "/refactorWithComments/{configID}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Perform refactorings with Pull-Request-Comments.")
	public ResponseEntity<?> refactorWithComments(@PathVariable Long configID) {

		// Create empty list of refactored Issues
		List<RefactoredIssue> allRefactoredIssues = new ArrayList<RefactoredIssue>();

		// Try to get the Git-Configuration with the given ID
		Optional<GitConfiguration> gitConfig = configRepo.getByID(configID);
		// If Configuration does not exist
		if (!gitConfig.isPresent()) {
			return new ResponseEntity<String>("Configuration with given ID does not exist!", HttpStatus.NOT_FOUND);
		}

		// Init translated Pull-Request-Object
		BotPullRequests allRequests = null;
		try {
			// Fetch target-Repository-Data
			dataGetter.fetchRemote(gitConfig.get());
			// Get Pull-Requests with comments
			allRequests = grabber.getRequestsWithComments(gitConfig.get());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// Iterate through all requests
		for (BotPullRequest request : allRequests.getAllPullRequests()) {
			// Iterate through all comments
			for (BotPullRequestComment comment : request.getAllComments()) {
				// Check if comment is valid and not already refactored
				if (grammarController.checkComment(comment.getCommentBody()) && !issueRepo
						.refactoredComment(gitConfig.get().getRepoService(), comment.getCommentID().toString())
						.isPresent()) {
					// Create issue
					BotIssue botIssue;
					try {
						botIssue = grammarController.createIssueFromComment(comment);
					} catch (Exception e) {
						return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
					}


					try {
						// For Requests created by someone else
						if (!request.getCreatorName().equals(gitConfig.get().getBotName())) {
							// Create refactoring branch with Filehoster-Service + Comment-ID
							String newBranch = gitConfig.get().getRepoService() + "_Refactoring_"
									+ comment.getCommentID().toString();
							dataGetter.createBranch(gitConfig.get(), request.getBranchName(), newBranch);

							// Try to refactor
							String commitMessage = refactoring.pickAndRefactor(botIssue, gitConfig.get());

							// If successful
							if (commitMessage != null) {
								// Create Refactored-Obect
								RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue,
										gitConfig.get());

								// Save to Database + add to list
								RefactoredIssue savedIssue = refactoredIssues.save(refactoredIssue);
								allRefactoredIssues.add(savedIssue);

								// Push changes + create Pull-Request
								dataGetter.pushChanges(gitConfig.get(), commitMessage);
								grabber.makeCreateRequest(request, comment, gitConfig.get(), newBranch);
							}
							// For Requests created by the bot
						} else {
							// Change to existing Refactoring-Branch
							dataGetter.switchBranch(gitConfig.get(), request.getBranchName());

							// Try to refactor
							String commitMessage = refactoring.pickAndRefactor(botIssue, gitConfig.get());

							// If successful
							if (commitMessage != null) {
								// Create Refactored-Object
								RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue,
										gitConfig.get());

								// Save to Database + add to list
								RefactoredIssue savedIssue = refactoredIssues.save(refactoredIssue);
								allRefactoredIssues.add(savedIssue);

								// Push changes + edit Pull-Request
								dataGetter.pushChanges(gitConfig.get(), commitMessage);
								grabber.makeUpdateRequest(request, comment, gitConfig.get());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
					}
				}
			}
		}

		// Return all refactored issues
		return new ResponseEntity<List<RefactoredIssue>>(allRefactoredIssues, HttpStatus.OK);
	}

	/**
	 * This method performs refactorings according to findings with an analysis
	 * service like SonarCube.
	 * 
	 * @param configID
	 * @return allRefactoredIssues
	 */
	@RequestMapping(value = "/refactorWithAnalysisService/{configID}", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Perform refactorings with analysis service.")
	public ResponseEntity<?> refactorWithSonarCube(@PathVariable Long configID) {

		// Create empty list of refactored Issues
		List<RefactoredIssue> allRefactoredIssues = new ArrayList<RefactoredIssue>();

		// Try to get the Git-Configuration with the given ID
		Optional<GitConfiguration> gitConfig = configRepo.getByID(configID);
		// If configuration does not exist
		if (!gitConfig.isPresent()) {
			return new ResponseEntity<String>("Configuration with the given ID does not exist!", HttpStatus.NOT_FOUND);
		}
		// If analysis service data is missing
		if (gitConfig.get().getAnalysisService() == null || gitConfig.get().getAnalysisServiceProjectKey() == null) {
			return new ResponseEntity<String>("Configuration is missing analysis service data!",
					HttpStatus.BAD_REQUEST);
		}

		try {
			// Fetch target-Repository-data
			dataGetter.fetchRemote(gitConfig.get());
			// Check if max amount of Requests reached
			grabber.getRequestsWithComments(gitConfig.get());
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}

		try {
			// Get issues from analysis service API
			List<BotIssue> botIssues = grabber.getAnalysisServiceIssues(gitConfig.get());

			// If analysis-service not supported
			if (botIssues == null) {
				return new ResponseEntity<String>(
						"Analysis-Service '" + gitConfig.get().getAnalysisService() + "' is not supported!",
						HttpStatus.BAD_REQUEST);
			}

			// Iterate all issues
			for (BotIssue botIssue : botIssues) {
				// If issue was not already refactored
				if (!issueRepo.refactoredAnalysisIssue(botIssue.getCommentServiceID()).isPresent()) {
					// Create new branch for refactoring
					String newBranch = "sonarCube_Refactoring_" + botIssue.getCommentServiceID();
					dataGetter.createBranch(gitConfig.get(), "master", newBranch);
					// Try to refactor
					String commitMessage = refactoring.pickAndRefactor(botIssue, gitConfig.get());

					// If successful
					if (commitMessage != null) {
						// Create Refactored-Object
						RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, gitConfig.get());

						// Save to database + add to list
						RefactoredIssue savedIssue = refactoredIssues.save(refactoredIssue);
						allRefactoredIssues.add(savedIssue);

						// Push changes + create Pull-Request
						dataGetter.pushChanges(gitConfig.get(), commitMessage);
						grabber.makeCreateRequestWithAnalysisService(botIssue, gitConfig.get(), newBranch);
					}
				}
			}

			return new ResponseEntity<List<RefactoredIssue>>(allRefactoredIssues, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
