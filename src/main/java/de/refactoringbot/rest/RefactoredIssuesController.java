package de.refactoringbot.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.model.refactoredissue.RefactoredIssue;
import de.refactoringbot.model.refactoredissue.RefactoredIssueRepository;
import de.refactoringbot.services.main.RefactoredIssuesService;
import io.swagger.annotations.ApiOperation;

/**
 * This class offers an CRUD-Interface as a REST-API for the refactored issues.
 * 
 * @author Stefan Basaric
 *
 */
@RestController
@RequestMapping(path = "/refactored-issues")
public class RefactoredIssuesController {

	@Autowired
	RefactoredIssueRepository repo;
	@Autowired
	RefactoredIssuesService issuesService;

	private static final Logger logger = LoggerFactory.getLogger(RefactoredIssuesController.class);

	/**
	 * This method returns all refactored issues of a specific user from a specific
	 * filehoster.
	 * 
	 * @return allIssues
	 */

	@GetMapping(produces = "application/json")
	@ApiOperation(value = "Get all refactored issues. Can filter for filehoster and repository owner")
	public ResponseEntity<?> getAllIssues(@RequestParam(value = "repoService", required = false) String repoService,
			@RequestParam(value = "ownerName", required = false) String repoOwner) {
		try {
			Iterable<RefactoredIssue> allIssues = issuesService.getAllIssues(repoService, repoOwner);
			return new ResponseEntity<>(allIssues, HttpStatus.OK);
		} catch (DatabaseConnectionException e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * This method deletes all refactored issues from the database.
	 * 
	 * @return feedback
	 */
	@DeleteMapping(produces = "application/json")
	@ApiOperation(value = "This method deletes all refactored issues from the database (for testing purposes).")
	public ResponseEntity<?> deleteAllRefactoredIssues() {
		try {
			issuesService.deleteAllIssues();
			return new ResponseEntity<>("All refactored issues deleted!", HttpStatus.OK);
		} catch (DatabaseConnectionException e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
