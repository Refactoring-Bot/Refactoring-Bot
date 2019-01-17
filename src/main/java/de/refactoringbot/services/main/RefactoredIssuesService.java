package de.refactoringbot.services.main;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.model.refactoredissue.RefactoredIssue;
import de.refactoringbot.model.refactoredissue.RefactoredIssueRepository;

/**
 * This class contains functions regarding the issues that were already
 * refactored by a bot.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class RefactoredIssuesService {

	@Autowired
	RefactoredIssueRepository repo;

	private static final Logger logger = LoggerFactory.getLogger(RefactoredIssuesService.class);

	/**
	 * This method returns all issues filtered by the filehosting service, the
	 * service user or both.
	 * 
	 * @param repoService
	 * @param repoOwner
	 * @return allIssues
	 * @throws DatabaseConnectionException
	 */
	public Iterable<RefactoredIssue> getAllIssues(String repoService, String repoOwner)
			throws DatabaseConnectionException {
		Iterable<RefactoredIssue> allIssues;
		try {
			if (repoService == null) {
				allIssues = repo.findAll();
			} else if (repoOwner == null) {
				allIssues = repo.getAllServiceRefactorings(repoService);
			} else {
				allIssues = repo.getAllUserIssues(repoService, repoOwner);
			}
			return allIssues;
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			throw new DatabaseConnectionException("Connection with database failed!");
		}
	}

	/**
	 * This method deletes all refactored issues from the database.
	 * 
	 * @throws DatabaseConnectionException
	 */
	public void deleteAllIssues() throws DatabaseConnectionException {
		try {
			repo.deleteAll();
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			throw new DatabaseConnectionException("Connection with database failed!");
		}
	}
}
