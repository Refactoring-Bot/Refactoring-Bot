package de.refactoringbot.services.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(BotService.class);

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

	/**
	 * This method finds the Root-Folder of a repository. That is the folder that
	 * contains the src folder.
	 * 
	 * @param repoFolder
	 * @return rootFolder
	 * @throws IOException
	 */
	public String findSrcFolder(String repoFolder) throws IOException {
		// Get root folder of project
		File dir = new File(repoFolder);

		// Get paths to all java files of the project
		List<File> files = (List<File>) FileUtils.listFilesAndDirs(dir, TrueFileFilter.INSTANCE,
				TrueFileFilter.INSTANCE);
		for (File file : files) {
			if (file.isDirectory() && file.getName().equals("src")) {
				return file.getAbsolutePath();
			}
		}
		
		String noSrcFolderFoundErrorMsg = "No src-folder found inside this java-project!";
		logger.error(noSrcFolderFoundErrorMsg);
		throw new FileNotFoundException(noSrcFolderFoundErrorMsg);
	}
}
