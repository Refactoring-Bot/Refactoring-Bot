package de.refactoringbot.services.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

import de.refactoringbot.api.github.GithubDataGrabber;
import de.refactoringbot.model.botissuegroup.BotIssueGroup;
import de.refactoringbot.model.botissuegroup.BotIssueGroupType;
import de.refactoringbot.model.exceptions.*;
import de.refactoringbot.model.refactoredissuegroup.RefactoredIssueGroup;
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
	ApiGrabber apiGrabber;
	@Autowired
	SonarQubeDataGrabber sonarQubeGrabber;
	@Autowired
	GitService gitService;
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
	@Autowired
	FileService fileService;
	@Autowired
	GithubDataGrabber githubGrabber;

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
			return processComments(config, allRequests);
		} else {
			return processAnalysisIssues(config, amountOfBotRequests);
		}
	}

	/**
	 * This method processes the refactoring of issues detected by an analysis
	 * service.
	 *
	 * @param config
	 *            param allIssues
	 * @return response
	 */
	private ResponseEntity<?> processAnalysisIssues(GitConfiguration config, int amountBotRequests) {

		if (amountBotRequests >= config.getMaxAmountRequests()) {
			return new ResponseEntity<String>(
					"The maximum number of open pull requests created by the Bot has been reached!",
					HttpStatus.BAD_REQUEST);
		}

		List<RefactoredIssue> allRefactoredIssues = new ArrayList<>();
		RefactoredIssueGroup allRefactoredIssueGroup;
		List<RefactoredIssueGroup> groupsOfRefactoredIssues = new ArrayList<>();
		RefactoredIssueGroup refactoredIssueGroup;

		try {
			// Get issues from analysis service API
			List<BotIssue> botIssues = apiGrabber.getAnalysisServiceIssues(config);
			// because of trouble with the CommentedOutCode refactorings this one are saved
			// in a separate list and prioritised on its own.
			// later they will be added to the botIssue list again.
			List<BotIssue> commentedOutIssues = new ArrayList<>();

			for (BotIssue issue : botIssues) {
				// TODO: später mit richtigem branch name arbeiten
				issue.setCountChanges(gitService.countCommitsFromHistory(issue, config, "master"));

				if (issue.getRefactoringOperation().equals("Remove Commented Out Code")) {
					commentedOutIssues.add(issue);
				}
			}

			botIssues.removeAll(commentedOutIssues);
			botIssues = bubbleSort(botIssues);
			commentedOutIssues = sortCommentedOutIssues(commentedOutIssues);
			botIssues.addAll(commentedOutIssues);

			List<BotIssueGroup> issueGroups = grouping(botIssues);
			issueGroups = groupPrioritization(issueGroups);

			// Iterate all issues
			for (BotIssueGroup botIssueGroup : issueGroups) {
				allRefactoredIssues.clear();
				refactoredIssueGroup = new RefactoredIssueGroup();

				// When Bot-Pull-Request-Limit reached -> return
				if (amountBotRequests >= config.getMaxAmountRequests()) {
					// Return all refactored issues
					return new ResponseEntity<>(groupsOfRefactoredIssues, HttpStatus.OK);
				}

				try {
					// If issue was not already refactored
					if (isAnalysisIssueValid(botIssueGroup) && botIssueGroup.getBotIssueGroup().size() > 0) {
						// Perform refactoring
						allRefactoredIssueGroup = refactorIssue(false, config, null, null, botIssueGroup);
						for (RefactoredIssue issue : allRefactoredIssueGroup.getRefactoredIssueGroup()) {
							allRefactoredIssues.add(issue);
						}
					}
				} catch (Exception e) {
					// Create failed Refactored-Object
					botIssueGroup.addIssue(new BotIssue());
					botIssueGroup.getBotIssueGroup().get(0)
							.setErrorMessage("Bot could not refactor this issue! Internal server error!");
					allRefactoredIssueGroup = processFailedRefactoring(config, null, null, botIssueGroup, false);
					for (RefactoredIssue issue : allRefactoredIssueGroup.getRefactoredIssueGroup()) {
						allRefactoredIssues.add(issue);
					}
					logger.error(e.getMessage(), e);
				}
				refactoredIssueGroup.addIssues(allRefactoredIssues);
				groupsOfRefactoredIssues.add(refactoredIssueGroup);
				amountBotRequests++; // the amountBotRequest is only incremented when a group of refactorings are
										// pushed.
			}

			return new ResponseEntity<>(groupsOfRefactoredIssues, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * Sort a list with the bubble sort with its value on countChanges. After this
	 * method the BotIssue with the highest value on countChanges will be the first
	 * in the List.
	 *
	 * @param list
	 * @return list, the sorted list
	 */
	private List<BotIssue> bubbleSort(List<BotIssue> list) {
		BotIssue temp;

		for (int i = 0; i < list.size() - 2; i++) {
			for (int j = 0; j < list.size() - i - 1; j++) {
				if (list.get(j).getCountChanges() < list.get(j + 1).getCountChanges()) {
					temp = list.get(j);
					list.set(j, list.get(j + 1));
					list.set(j + 1, temp);
				}
			}
		}

		return list;
	}

	/**
	 * Sorts the commentedOut Issues that the issue with the highest line is on the
	 * top
	 *
	 * @param issues
	 * @return
	 */
	private List<BotIssue> sortCommentedOutIssues(List<BotIssue> issues) {
		BotIssue temp;

		for (int i = 0; i < issues.size() - 2; i++) {
			for (int j = 0; j < issues.size() - i - 1; j++) {
				if (issues.get(j).getLine() < issues.get(j + 1).getLine()) {
					temp = issues.get(j);
					issues.set(j, issues.get(j + 1));
					issues.set(j + 1, temp);
				}
			}
		}

		return issues;
	}

	/**
	 * This method groups the prioritised Bot-Issues.
	 *
	 * @param prioList
	 */
	public List<BotIssueGroup> grouping(List<BotIssue> prioList) throws BotIssueTypeException {
		List<BotIssueGroup> issueGroups = new ArrayList<>();
		boolean found;

		// order each BotIssue to a group
		for (BotIssue issue : prioList) {
			found = false;
			try {
				for (BotIssueGroup group : issueGroups) {
					// check if there is already a group for the class and refactoring type
					if (group.getName().equals(issue.getFilePath()) && group.getBotIssueGroup().get(0)
							.getRefactoringOperation().equals(issue.getRefactoringOperation())) {
						// find the right group size
						if (!issue.getRefactoringOperation().equals(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE)
								&& group.getBotIssueGroup().size() < 20) {
							group.addIssue(issue);
							found = true;
							break;
						} else if (issue.getRefactoringOperation()
								.equals(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE)
								&& group.getBotIssueGroup().size() < 10) {
							group.addIssue(issue);
							found = true;
							break;
						}
					}

				}
				if (!found) {
					BotIssueGroup newGroup = new BotIssueGroup(BotIssueGroupType.CLASS, issue.getFilePath());
					newGroup.addIssue(issue);
					issueGroups.add(newGroup);
				}

			} catch (NullPointerException e) {
				logger.error(e.getMessage(), e);
			}

		}

		return issueGroups;
	}

	/**
	 * This method prioritises the Bot-Issue-Group. It is a simple sort with the sum
	 * of count-changes of the botIssues in the group.
	 */
	private List<BotIssueGroup> groupPrioritization(List<BotIssueGroup> issueGroups) {
		BotIssueGroup temp;

		for (int i = 0; i < issueGroups.size() - 2; i++) {
			for (int j = 0; j < issueGroups.size() - i - 1; j++) {
				if (issueGroups.get(j).getValueCounChange() < issueGroups.get(j + 1).getValueCounChange()) {
					temp = issueGroups.get(j);
					issueGroups.set(j, issueGroups.get(j + 1));
					issueGroups.set(j + 1, temp);
				}
			}
		}

		return issueGroups;
	}

	/**
	 * This method processes the comment driven refactoring.
	 *
	 * @param config
	 * @param allRequests
	 * @return response
	 */
	private ResponseEntity<?> processComments(GitConfiguration config, BotPullRequests allRequests) {
		List<RefactoredIssue> allRefactoredIssues = new ArrayList<>();

		for (BotPullRequest request : allRequests.getAllPullRequests()) {
			// Check only PR's of the Bot
			if (request.getCreatorName().equals(config.getBotName())) {
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
								botIssue = witService.createBotIssue(comment);
								logger.info("Comment translated with 'wit.ai': {}", comment.getCommentBody());
							} catch (ReviewCommentUnclearException | WitAPIException e) {
								logger.warn("Comment translation with 'wit.ai' failed! Comment: "
										+ comment.getCommentBody());
								botIssue = createBotIssueFromInvalidComment(comment, e.getMessage());
								allRefactoredIssues
										.add(processFailedRefactoring(config, comment, request, botIssue, true));
								continue;
							}
						} else {
							// Try to refactor with ANTRL4
							try {
								// If ANTLR can parse -> create Issue
								botIssue = grammarService.createIssueFromComment(comment, config);
								logger.info("Comment translated with 'ANTLR': {}", comment.getCommentBody());
							} catch (Exception g) {
								logger.error(g.getMessage(), g);
								// If refactoring failed
								botIssue = createBotIssueFromInvalidComment(comment, g.getMessage());
								allRefactoredIssues
										.add(processFailedRefactoring(config, comment, request, botIssue, true));
								continue;
							}
						}
						// Refactor the created BotIssue
						allRefactoredIssues.add(refactorComment(config, botIssue, request, comment));
					}
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
	 *            param issue
	 * @param request
	 * @param comment
	 *            param amountBotRequests
	 * @return refactoredIssue
	 */
	private RefactoredIssue refactorComment(GitConfiguration config, BotIssue botIssue, BotPullRequest request,
			BotPullRequestComment comment) {

		RefactoredIssue refactoredIssue = null;

		try {
			// Perform refactoring
			refactoredIssue = refactorIssue(true, config, comment, request, botIssue);
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
	 * @param isCommentRefactoring
	 * @param config
	 * @param comment
	 * @param request
	 * @param botIssue
	 * @return allRefactoredIssues
	 * @throws Exception
	 */
	private RefactoredIssue refactorIssue(boolean isCommentRefactoring, GitConfiguration config,
			BotPullRequestComment comment, BotPullRequest request, BotIssue botIssue) throws Exception {
		// If refactoring via comment
		if (isCommentRefactoring) {
			// Change to existing Refactoring-Branch
			gitService.switchBranch(config, request.getBranchName());

			// Add current filepaths to Issue
			botIssue = addUpToDateFilePaths(botIssue, isCommentRefactoring, config);

			// Try to refactor
			botIssue.setCommitMessage(refactoring.pickAndRefactor(botIssue, config));

			// If successful
			if (botIssue.getCommitMessage() != null) {
				// Create Refactored-Object
				RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, config);
				// Push changes
				gitService.commitAndPushChanges(config, botIssue.getCommitMessage());
				// Reply to User
				apiGrabber.replyToUserInsideBotRequest(request, comment, config);

				// Save and return refactored issue
				return issueRepo.save(refactoredIssue);
			}

			// If analysis service refactoring
		} else {
			// Create new branch for refactoring
			String newBranch = "sonarQube_Refactoring_" + botIssue.getCommentServiceID();
			// Check if branch already exists (throws exception if it does)
			apiGrabber.checkBranch(config, newBranch);
			gitService.createBranch(config, "master", newBranch, "upstream");
			// Add current filepaths to Issue
			botIssue = addUpToDateFilePaths(botIssue, isCommentRefactoring, config);
			// Try to refactor
			botIssue.setCommitMessage(refactoring.pickAndRefactor(botIssue, config));

			// If successful
			if (botIssue.getCommitMessage() != null) {
				// Create Refactored-Object
				RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, config);

				// Push changes + create Pull-Request
				gitService.commitAndPushChanges(config, botIssue.getCommitMessage());
				apiGrabber.makeCreateRequestWithAnalysisService(botIssue, config, newBranch);

				// Save and return refactored issue
				return issueRepo.save(refactoredIssue);
			}
		}

		botIssue.setErrorMessage("Could not create a commit message!");
		return processFailedRefactoring(config, comment, request, botIssue, isCommentRefactoring);
	}

	/**
	 * This method configures the local workspace, refactors the issue, pushes the
	 * changes and creates an PR. This method uses the BotIssueGroup to create
	 * Pull-Requests
	 *
	 * @param isCommentRefactoring
	 * @param config
	 * @param comment
	 * @param request
	 * @param botIssueGroup
	 * @return allRefactoredIssues
	 * @throws Exception
	 */
	private RefactoredIssueGroup refactorIssue(boolean isCommentRefactoring, GitConfiguration config,
			BotPullRequestComment comment, BotPullRequest request, BotIssueGroup botIssueGroup) throws Exception {
		RefactoredIssueGroup refactoredIssueGroup = new RefactoredIssueGroup();
		// If refactoring via comment
		// TODO: I'm not sure if this is working
		if (isCommentRefactoring) {
			BotIssue botIssue = botIssueGroup.getBotIssueGroup().get(0);
			// Change to existing Refactoring-Branch
			gitService.switchBranch(config, request.getBranchName());

			// Add current filepaths to Issue
			botIssue = addUpToDateFilePaths(botIssue, isCommentRefactoring, config);

			// Try to refactor
			botIssue.setCommitMessage(refactoring.pickAndRefactor(botIssue, config));

			// If successful
			if (botIssue.getCommitMessage() != null) {
				// Create Refactored-Object
				RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, config);
				// Push changes
				gitService.commitAndPushChanges(config, botIssue.getCommitMessage());
				// Reply to User
				apiGrabber.replyToUserInsideBotRequest(request, comment, config);

				// Save and return refactored issue
				issueRepo.save(refactoredIssue);
			}

			// If analysis service refactoring
		} else {
			// Create new branch for refactoring
			String newBranch = "sonarQube_Refactoring_Group_"
					+ botIssueGroup.getBotIssueGroup().get(0).getCommentServiceID();
			// Check if branch already exists (throws exception if it does)
			apiGrabber.checkBranch(config, newBranch);
			gitService.createBranch(config, "master", newBranch, "upstream");
			// Add current filepaths to Issue
			for (BotIssue botIssue : botIssueGroup.getBotIssueGroup()) {
				botIssue = addUpToDateFilePaths(botIssue, isCommentRefactoring, config);
				// Try to refactor
				botIssue.setCommitMessage(refactoring.pickAndRefactor(botIssue, config));

				try {
					// If successful
					if (botIssue.getCommitMessage() != null) {
						// Create Refactored-Object
						RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, config);
						refactoredIssueGroup.addIssue(refactoredIssue);
						issueRepo.save(refactoredIssue);
						gitService.commitAndPushChanges(config, botIssue.getCommitMessage());
					} else {
						botIssue.setErrorMessage("Could not create a commit message!");
						return processFailedRefactoring(config, comment, request, botIssueGroup, isCommentRefactoring);
					}
				} catch (Exception e) {
					botIssueGroup.remove(botIssue);
				}
			}

			// Push changes + create Pull-Request
			apiGrabber.makeCreateRequestWithAnalysisService(botIssueGroup, config, newBranch);
		}

		// Save and return refactored issue
		return refactoredIssueGroup;
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
	 * @return
	 * @throws URISyntaxException
	 * @throws GitHubAPIException
	 * @throws IOException
	 * @throws GitWorkflowException
	 * @throws GitLabAPIException
	 */
	public BotPullRequests getPullRequests(GitConfiguration config)
			throws URISyntaxException, GitHubAPIException, IOException, GitWorkflowException, GitLabAPIException {
		gitService.fetchRemote(config);
		return apiGrabber.getRequestsWithComments(config);
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
	 * param config
	 * 
	 * @param issue
	 * @return
	 */
	public boolean isAnalysisIssueValid(BotIssue issue) {
		return (!issueRepo.refactoredAnalysisIssue(issue.getCommentServiceID()).isPresent());
	}

	/**
	 * This method checks if a from a AnalysisServiceIssue translated BotIssue is
	 * valid, e.g. not already refactored.
	 *
	 * param config
	 * 
	 * @param group
	 * @return
	 */
	public boolean isAnalysisIssueValid(BotIssueGroup group) {
		for (BotIssue issue : group.getBotIssueGroup()) {
			if (issueRepo.refactoredAnalysisIssue(issue.getCommentServiceID()).isPresent()) {
				return false;
			}
		}
		return true;
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
	 * @param isCommentRefactoring
	 * @return failedIssue
	 */
	private RefactoredIssue processFailedRefactoring(GitConfiguration config, BotPullRequestComment comment,
			BotPullRequest request, BotIssue botIssue, boolean isCommentRefactoring) {
		// Create failedIssue
		RefactoredIssue failedIssue = botController.buildRefactoredIssue(botIssue, config);

		// Reply to user if refactoring comments
		if (isCommentRefactoring) {
			try {
				apiGrabber.replyToUserForFailedRefactoring(request, comment, config,
						constructCommentReplyMessage(botIssue.getErrorMessage()));
			} catch (Exception u) {
				logger.error(u.getMessage(), u);
			}
		}

		// Save failed refactoring and return it
		return issueRepo.save(failedIssue);
	}

	/**
	 * This method processes a faild refactoring. It creates a RefactoredIssue
	 * object and saves it to the database so that the bot won't try to refactor a
	 * comment that can not be refactored. Also, a reply is sent to the comment
	 * creator to inform him of the failure with a proper error message. This method
	 * uses the BotIssueGroup
	 *
	 * @param config
	 * @param comment
	 * @param request
	 * @param botIssueGroup
	 * @param isCommentRefactoring
	 * @return failedIssue
	 */
	private RefactoredIssueGroup processFailedRefactoring(GitConfiguration config, BotPullRequestComment comment,
			BotPullRequest request, BotIssueGroup botIssueGroup, boolean isCommentRefactoring) {
		// Create failedIssue
		RefactoredIssue failedIssue = botController.buildRefactoredIssue(botIssueGroup.getBotIssueGroup().get(0),
				config);// TODO: noch richtig anpassen

		// Reply to user if refactoring comments
		if (isCommentRefactoring) {
			try {
				apiGrabber.replyToUserForFailedRefactoring(request, comment, config,
						constructCommentReplyMessage(botIssueGroup.getBotIssueGroup().get(0).getErrorMessage()));
			} catch (Exception u) {
				logger.error(u.getMessage(), u);
			}
		}
		issueRepo.save(failedIssue);
		RefactoredIssueGroup group = new RefactoredIssueGroup();
		group.addIssue(failedIssue);

		// Save failed refactoring and return it
		return group;
	}

	/**
	 * This method creates a reply message with the help from the error message of a
	 * BotRefactoringException.
	 *
	 * @param exceptionMessage
	 * @return replyMessage
	 */
	private String constructCommentReplyMessage(String exceptionMessage) {
		return exceptionMessage + " Please rephrase your instruction in a new comment.";
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
	 * This method updates a BotIssue with up-to-date file paths.
	 *
	 * @param botIssue
	 * @param isCommentRefactoring
	 * @param config
	 * @return botIssue
	 * @throws IOException
	 */
	private BotIssue addUpToDateFilePaths(BotIssue botIssue, Boolean isCommentRefactoring, GitConfiguration config)
			throws IOException {
		botIssue.setAllJavaFiles(fileService.getAllJavaFiles(config.getRepoFolder()));
		botIssue.setJavaRoots(fileService.findJavaRoots(botIssue.getAllJavaFiles()));

		if (!isCommentRefactoring) {
			botIssue.setFilePath(apiGrabber.getAnalysisServiceAbsoluteIssuePath(config, botIssue.getFilePath()));
		}

		return botIssue;
	}
}
