package de.refactoringbot.services.main;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.springframework.stereotype.Service;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.output.botpullrequest.BotPullRequest;
import de.refactoringbot.model.output.botpullrequest.BotPullRequests;
import de.refactoringbot.model.output.botpullrequestcomment.BotPullRequestComment;
import de.refactoringbot.model.refactoredissue.RefactoredIssue;

/**
 * This class performs bot specific operations.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class BotService {

	/**
	 * This method returns the maximal amount of pull requests created by the bot.
	 * 
	 * @param requests
	 * @param gitConfig
	 * @throws Exception
	 */
	public Integer getAmountOfBotRequests(BotPullRequests requests, GitConfiguration gitConfig) {

		// Init counter
		int counter = 0;
		// Iterate requests
		for (BotPullRequest request : requests.getAllPullRequests()) {
			// If request belongs to bot
			if (request.getCreatorName().equals(gitConfig.getBotName())) {
				counter++;
			}
		}

		return counter;
	}

	/**
	 * The Method creates a RefactoredIssue-Object from a
	 * Analysis-Service-Refactoring.
	 * 
	 * @param issue
	 * @param gitConfig
	 * @return refactoredIssue
	 */
	public RefactoredIssue buildRefactoredIssue(BotIssue issue, GitConfiguration gitConfig) {
		// Create object
		RefactoredIssue refactoredIssue = new RefactoredIssue();

		// Create timestamp
		SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date now = new Date();
		String date = sdf.format(now);

		// Fill object
		refactoredIssue.setCommentServiceID(issue.getCommentServiceID());
		refactoredIssue.setRepoName(gitConfig.getRepoName());
		refactoredIssue.setRepoOwner(gitConfig.getRepoOwner());
		refactoredIssue.setRepoService(gitConfig.getRepoService());
		refactoredIssue.setDateOfRefactoring(date);
		refactoredIssue.setAnalysisService(gitConfig.getAnalysisService());
		refactoredIssue.setAnalysisServiceProjectKey(gitConfig.getAnalysisServiceProjectKey());
		refactoredIssue.setRefactoringOperation(issue.getRefactoringOperation());
		refactoredIssue.setCountChanges(issue.getCountChanges());

		if (issue.getCommitMessage() != null && issue.getErrorMessage() == null) {
			refactoredIssue.setStatus("SUCCESSFUL");
		} else {
			refactoredIssue.setStatus("FAILED");
		}

		return refactoredIssue;
	}

	/**
	 * The Method creates a RefactoredIssue-Object from a
	 * Request-Comment-Refactoring.
	 * 
	 * @param gitConfig
	 * @return refactoredIssue
	 */
	public RefactoredIssue buildRefactoredIssue(BotPullRequest request, BotPullRequestComment comment,
			GitConfiguration gitConfig) {
		// Create object
		RefactoredIssue refactoredIssue = new RefactoredIssue();

		// Create timestamp
		SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSSZ");
		Date now = new Date();
		String date = sdf.format(now);

		// Fill object
		refactoredIssue.setCommentServiceID(comment.getCommentID().toString());
		refactoredIssue.setRepoName(gitConfig.getRepoName());
		refactoredIssue.setRepoOwner(gitConfig.getRepoOwner());
		refactoredIssue.setRepoService(gitConfig.getRepoService());
		refactoredIssue.setDateOfRefactoring(date);

		if (gitConfig.getAnalysisService() != null) {
			refactoredIssue.setAnalysisService(gitConfig.getAnalysisService());
		}

		if (gitConfig.getAnalysisServiceProjectKey() != null) {
			refactoredIssue.setAnalysisServiceProjectKey(gitConfig.getAnalysisServiceProjectKey());
		}

		return refactoredIssue;
	}
        
}
