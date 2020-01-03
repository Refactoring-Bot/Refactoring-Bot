package de.refactoringbot.services.main;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;

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
		//TODO: evtl wichtig für Gruppenprio
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
	 * param allIssues
	 * @return response
	 */
	private ResponseEntity<?> processAnalysisIssues(GitConfiguration config, int amountBotRequests) {

		if (amountBotRequests >= config.getMaxAmountRequests()) {
            return new ResponseEntity<String>(
                    "The maximum number of open pull requests created by the Bot has been reached!",
                    HttpStatus.BAD_REQUEST);
		}

		List<RefactoredIssue> allRefactoredIssues = new ArrayList<>();//TODO: rausfinden wo das überall verwendet wird, damit nachher die gruppen einen pullrequest bekommen und mein code keine auswirkungen auf andere stellen hat
		RefactoredIssueGroup allRefactoredIssueGroup;
		List<RefactoredIssueGroup> groupsOfRefactoredIssues = new ArrayList<>();
		RefactoredIssueGroup refactoredIssueGroup;

		try {
			// Get issues from analysis service API
			List<BotIssue> botIssues = apiGrabber.getAnalysisServiceIssues(config);
			//TODO: mit den issueGroups weiterarbeiten und code genau anschauen und anpassen
				List<BotIssueGroup> issueGroups = grouping(botIssues);
				//issueGroups = groupPrioritization(issueGroups); evtl erst die refactored groups priorisieren

			// Iterate all issues
			for (BotIssueGroup botIssueGroup : issueGroups) {
				allRefactoredIssues.clear(); //TODO: schauen, dass das keine negativen auswirkungen hat
				refactoredIssueGroup = new RefactoredIssueGroup();

				// When Bot-Pull-Request-Limit reached -> return
				if (amountBotRequests >= config.getMaxAmountRequests()) {
					// Return all refactored issues
					//return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);//TODO: rausfinden was die ResponseEntity ist und evtl durch die gruppen ersetzten
					return new ResponseEntity<>(groupsOfRefactoredIssues, HttpStatus.OK);
				}

				//for (BotIssue botIssue : issueList){
						try {
								//System.out.println("BotIssue Operation: " + botIssueGroup.getBotIssueGroup().get(0).getRefactoringOperation());
								// If issue was not already refactored
								if (isAnalysisIssueValid(botIssueGroup) && botIssueGroup.getBotIssueGroup().size() > 0) {
										// Perform refactoring
										allRefactoredIssueGroup = refactorIssue(false, config, null, null, botIssueGroup);
										System.out.println(botIssueGroup.getBotIssueGroup().size());
										for (RefactoredIssue issue : allRefactoredIssueGroup.getRefactoredIssueGroup()){
												allRefactoredIssues.add(issue);
										}
										//amountBotRequests++; Gilt nur wenn für einzelne issues pull request erstellt wird
								}
						} catch (Exception e) {
								// Create failed Refactored-Object
								botIssueGroup.addIssue(new BotIssue());
								botIssueGroup.getBotIssueGroup().get(0).setErrorMessage("Bot could not refactor this issue! Internal server error!");
								allRefactoredIssueGroup = processFailedRefactoring(config, null, null, botIssueGroup, false);
								for (RefactoredIssue issue : allRefactoredIssueGroup.getRefactoredIssueGroup()){
										allRefactoredIssues.add(issue);
								}
								logger.error(e.getMessage(), e);
						}
				//}
					refactoredIssueGroup.addIssues(allRefactoredIssues);
					groupsOfRefactoredIssues.add(refactoredIssueGroup);
					amountBotRequests++; //gilt wenn für gruppen pull requests erstellt werden
			}

			//return new ResponseEntity<>(allRefactoredIssues, HttpStatus.OK);
				return new ResponseEntity<>(groupsOfRefactoredIssues, HttpStatus.OK);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

		/**
		 * This method groups the prioritised Bot-Issues
		 * TODO: beschreiben wie du gruppierst
		 * TODO: Rückgabewert ändern und auf private setzten
		 * @param prioList
		 */
	public List<BotIssueGroup> grouping(List<BotIssue> prioList) throws BotIssueTypeException {
		List<BotIssueGroup> issueGroups = new ArrayList<>();
		List<BotIssueGroup> classGroupList = new ArrayList<>();
		BotIssueGroup addOverride = new BotIssueGroup(BotIssueGroupType.REFACTORING);
		//BotIssueGroup rename = new BotIssueGroup(BotIssueGroupType.REFACTORING);
		//BotIssueGroup reorder = new BotIssueGroup(BotIssueGroupType.REFACTORING);
		BotIssueGroup removeCom = new BotIssueGroup(BotIssueGroupType.REFACTORING);
		//BotIssueGroup removePar = new BotIssueGroup(BotIssueGroupType.REFACTORING);
		BotIssueGroup unknown = new BotIssueGroup(BotIssueGroupType.REFACTORING);
		BotIssueGroup classGroup;

		//TODO: bei probedurchlauf checken ob liste sortiert ist

			//order each BotIssue to a group
			for (BotIssue issue : prioList){
				if (issue.getRefactoringOperation().equals(RefactoringOperations.ADD_OVERRIDE_ANNOTATION)){
						//when the amount of refactorings in the addOverride group is bigger than 20,
						// then add the group to the issues and create a new group
						if (addOverride.getBotIssueGroup().size() >= 20){
								issueGroups.add(addOverride);
								addOverride = new BotIssueGroup(BotIssueGroupType.REFACTORING);
								addOverride.addIssue(issue);
						}else {
								addOverride.addIssue(issue);
						}
				} else if(issue.getRefactoringOperation().equals(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE)){
						//when the amount of refactorings in the commentedOutCode group is bigger than 10,
						// then add the group to the issues and create a new group
						if (removeCom.getBotIssueGroup().size() >= 10){
								issueGroups.add(removeCom);
								removeCom = new BotIssueGroup(BotIssueGroupType.REFACTORING);
								removeCom.addIssue(issue);
						}else {
								removeCom.addIssue(issue);
						}
				} else {
						//bei den anderen refactorings wird auf die klasse überprüft
						System.out.println(issue.getFilePath());
						boolean found = false;
						for (BotIssueGroup group : classGroupList){
								if (group.getName().equals(issue.getFilePath()) && group.getBotIssueGroup().size() < 20){//TODO: des mit den getAllJavaFiles rausfinden
										//group for same class and not full
										group.addIssue(issue);
										found = true;
										break;
								} else if (group.getName().equals(issue.getFilePath()) && group.getBotIssueGroup().size() >= 20){
										//group with same name but full, create a new group
										found = true;
										classGroup = new BotIssueGroup(BotIssueGroupType.CLASS);
										classGroup.setName(issue.getFilePath());
										classGroup.addIssue(issue);
										classGroupList.add(classGroup);
								}
						}
						if (!found){
								//no existing class group, create a new one and add it tho the list
								classGroup = new BotIssueGroup(BotIssueGroupType.CLASS);
								classGroup.setName(issue.getFilePath());
								classGroup.addIssue(issue);
								classGroupList.add(classGroup);
						}
				}
				/*else if(issue.getRefactoringOperation().equals(RefactoringOperations.RENAME_METHOD)){
						rename.addIssue(issue);
				} else if(issue.getRefactoringOperation().equals(RefactoringOperations.REORDER_MODIFIER)){
						reorder.addIssue(issue);
				} else if(issue.getRefactoringOperation().equals(RefactoringOperations.REMOVE_PARAMETER)){
						removePar.addIssue(issue);
				} else {
						//for the unknown group will be no limit
						unknown.addIssue(issue);
				}*/
				//TODO: bei listen auf größe überprüfen
			}

			//end of the alg check if a issue is in one of the list, then add it to the issueGroups
			if (addOverride.getBotIssueGroup().size() > 0){
					issueGroups.add(addOverride);
			}
			if (removeCom.getBotIssueGroup().size() > 0){
					issueGroups.add(removeCom);
			}
			if (classGroupList.size() > 0){
					issueGroups.addAll(classGroupList);
			}
			/*if (rename.getBotIssueGroup().size() > 0){
					issueGroups.add(rename);
			}
			if (reorder.getBotIssueGroup().size() > 0){
					issueGroups.add(reorder);
			}
			if (removePar.getBotIssueGroup().size() > 0){
					issueGroups.add(removePar);
			}*/
			if (unknown.getBotIssueGroup().size() > 0){
					issueGroups.add(unknown);
			}

			return issueGroups;
	}

		/**
		 * This method prioritises the Bot-Issue-Groups
		 * TODO: rückgabewert unb Parameter ändern
		 * TODO: beschreiben was du machst
		 */
	private void groupPrioritization(){

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
	 * param issue
	 * @param request
	 * @param comment
	 * param amountBotRequests
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
		 * changes and creates an PR.
		 * This method uses the BotIssueGroup to create Pull-Requests
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
				/*if (isCommentRefactoring) { TODO: evtl für commentedRefactoring implementeren
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
				} else {*/
				// Create new branch for refactoring
				String newBranch = "sonarQube_Refactoring_" + botIssueGroup.getBotIssueGroup().get(0).getCommentServiceID();
				// Check if branch already exists (throws exception if it does)
				apiGrabber.checkBranch(config, newBranch);
				gitService.createBranch(config, "master", newBranch, "upstream");
				// Add current filepaths to Issue
				for (BotIssue botIssue : botIssueGroup.getBotIssueGroup()) {
						botIssue = addUpToDateFilePaths(botIssue, isCommentRefactoring, config);
						// Try to refactor
						botIssue.setCommitMessage(refactoring.pickAndRefactor(botIssue, config));

						// If successful
						if (botIssue.getCommitMessage() != null) {
								// Create Refactored-Object
								RefactoredIssue refactoredIssue = botController.buildRefactoredIssue(botIssue, config);
								refactoredIssueGroup.addIssue(refactoredIssue);
								issueRepo.save(refactoredIssue); //TODO: vllt RefactoredIssueGroup so umschreiben dass es das auch speichert
						} else {
								botIssue.setErrorMessage("Could not create a commit message!");
								return processFailedRefactoring(config, comment, request, botIssueGroup, isCommentRefactoring);
						}
				}
				//}

				// Push changes + create Pull-Request
				gitService.commitAndPushChanges(config, botIssueGroup.getBotIssueGroup().get(0).getCommitMessage());
				apiGrabber.makeCreateRequestWithAnalysisService(botIssueGroup, config, newBranch);

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
		 * @param group
		 * @return
		 */
		public boolean isAnalysisIssueValid(BotIssueGroup group) {
				for (BotIssue issue : group.getBotIssueGroup()){
					if (issueRepo.refactoredAnalysisIssue(issue.getCommentServiceID()).isPresent()){
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
		 * creator to inform him of the failure with a proper error message.
		 * This method uses the BotIssueGroup
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
				RefactoredIssue failedIssue = botController.buildRefactoredIssue(botIssueGroup.getBotIssueGroup().get(0), config);//TODO: noch richtig anpassen

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
