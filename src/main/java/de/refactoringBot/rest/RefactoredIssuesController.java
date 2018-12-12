package de.refactoringBot.rest;

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

import de.refactoringBot.model.refactoredIssue.RefactoredIssue;
import de.refactoringBot.model.refactoredIssue.RefactoredIssueRepository;
import io.swagger.annotations.ApiOperation;

/**
 * This class offers an CRUD-Interface as a REST-API for the refactored issues.
 * 
 * @author Stefan Basaric
 *
 */
@RestController
@RequestMapping(path = "/refactoredIssues")
public class RefactoredIssuesController {

	@Autowired
	RefactoredIssueRepository repo;
	
	// Logger
    private static final Logger logger = LoggerFactory.getLogger(RefactoredIssuesController.class);

	/**
	 * This method returns all refactored issues from the database.
	 * 
	 * @return allIssues
	 */
	@GetMapping(value = "/getAllIssues", produces = "application/json")
	@ApiOperation(value = "Get all refactored issues.")
	public ResponseEntity<?> getAllIssues() {
		Iterable<RefactoredIssue> allIssues;
		try {
			allIssues = repo.findAll();
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Iterable<RefactoredIssue>>(allIssues, HttpStatus.OK);
	}

	/**
	 * This method returns all refactored issues of a filehoster from the database.
	 * 
	 * @return allIssues
	 */
	@GetMapping(value = "/getAllServiceIssues", produces = "application/json")
	@ApiOperation(value = "Get all refactored issues from a specific filehoster.")
	public ResponseEntity<?> getAllServiceIssues(
			@RequestParam(value = "repoService", required = true, defaultValue = "github") String repoService) {
		Iterable<RefactoredIssue> allIssues;
		try {
			allIssues = repo.getAllServiceRefactorings(repoService);
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Iterable<RefactoredIssue>>(allIssues, HttpStatus.OK);
	}

	/**
	 * This methor returns all refactored issues of a specific user from a specific
	 * filehoster.
	 * 
	 * @return allIssues
	 */
	@GetMapping(value = "/getAllUserIssues", produces = "application/json")
	@ApiOperation(value = "Get all refactored issues of a specific user from a specific filehoster.")
	public ResponseEntity<?> getAllUserIssues(
			@RequestParam(value = "repoService", required = true, defaultValue = "github") String repoService,
			@RequestParam(value = "ownerName", required = true, defaultValue = "LHommeDeBat") String repoOwner) {
		Iterable<RefactoredIssue> allIssues;
		try {
			allIssues = repo.getAllUserIssues(repoService, repoOwner);
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<Iterable<RefactoredIssue>>(allIssues, HttpStatus.OK);
	}

	/**
	 * This methor deletes all refactored issues from the database.
	 * 
	 * @return feedback
	 */
	@DeleteMapping(value = "/deleteAllIssues", produces = "application/json")
	@ApiOperation(value = "This method deletes all refactored issues from the database (for testing purposes).")
	public ResponseEntity<?> deleteAllRefactoredIssues() {
		try {
			repo.deleteAll();
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<String>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<String>("All refactored issues deleted!", HttpStatus.OK);
	}
}
