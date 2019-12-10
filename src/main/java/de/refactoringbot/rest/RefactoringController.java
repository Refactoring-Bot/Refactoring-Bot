package de.refactoringbot.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.refactoringbot.model.exceptions.BotRefactoringException;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.services.main.RefactoringService;
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
		// Refactor with comments and respond with result
		try {
				//TODO: beachten
			return refactoringService.performRefactoring(configID, true);
		} catch (DatabaseConnectionException d) {
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException n) {
			return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
		} catch (BotRefactoringException b) {
			return new ResponseEntity<>(b.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * This method performs refactorings according to findings with an analysis
	 * service like SonarQube.
	 * 
	 * @param configID
	 * @return allRefactoredIssues
	 */
	@PostMapping(value = "/{configID}/refactorWithAnalysisService", produces = "application/json")
	@ApiOperation(value = "Perform refactorings with analysis service.")
	public ResponseEntity<?> refactorWithSonarQube(@PathVariable Long configID) {
		// Refactor with analysis service and respond with result
		try {
				//TODO: beachten
			return refactoringService.performRefactoring(configID, false);
		} catch (DatabaseConnectionException d) {
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException n) {
			return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
		} catch (BotRefactoringException b) {
			return new ResponseEntity<>(b.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
}
