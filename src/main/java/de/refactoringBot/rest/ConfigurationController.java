package de.refactoringBot.rest;

import java.io.File;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.refactoringBot.api.main.ApiGrabber;
import de.refactoringBot.configuration.BotConfiguration;
import de.refactoringBot.controller.github.GithubObjectTranslator;
import de.refactoringBot.controller.main.GitController;
import de.refactoringBot.model.configuration.ConfigurationRepository;
import de.refactoringBot.model.configuration.GitConfiguration;
import io.swagger.annotations.ApiOperation;

/**
 * This method offers an CRUD-Interface as a REST-Interface for the
 * Git-Configurations.
 * 
 * @author Stefan Basaric
 *
 */
@RestController
@RequestMapping(path = "/gitConfiguration")
public class ConfigurationController {

	@Autowired
	ConfigurationRepository repo;
	@Autowired
	ApiGrabber grabber;
	@Autowired
	GithubObjectTranslator translator;
	@Autowired
	BotConfiguration botConfig;
	@Autowired
	GitController gitController;

	/**
	 * This method creates an git configuration with the user inputs.
	 * 
	 * @param repoName
	 * @param repoOwner
	 * @param repoService
	 * @return
	 */
	@RequestMapping(value = "/createConfig", method = RequestMethod.POST, produces = "application/json")
	@ApiOperation(value = "Create Git-Konfiguration")
	public ResponseEntity<?> add(
			@RequestParam(value = "repoService", required = true, defaultValue = "Github") String repoService,
			@RequestParam(value = "repoName", required = true, defaultValue = "RefactoringTest") String repoName,
			@RequestParam(value = "ownerName", required = true, defaultValue = "Refactoring-Bot") String repoOwner,
			@RequestParam(value = "ProjectRootFolder", required = true, defaultValue = "Calculator") String projectRootFolder,
			@RequestParam(value = "botUsername", required = true) String botUsername,
			@RequestParam(value = "botPassword", required = true) String botPassword,
			@RequestParam(value = "botEmail", required = true) String botEmail,
			@RequestParam(value = "botToken", required = true) String botToken,
			@RequestParam(value = "analysisService", required = false, defaultValue = "sonarqube") String analysisService,
			@RequestParam(value = "analysisServiceProjectKey", required = false) String analysisServiceProjectKey,
			@RequestParam(value = "maxAmountRequests", required = true, defaultValue = "5") Integer maxAmountRequests) {
		// Check if repository already exists in another configuration
		Optional<GitConfiguration> existsConfig = repo.getConfigByName(repoName, repoOwner);
		// If it does
		if (existsConfig.isPresent()) {
			return new ResponseEntity<String>("There is already an configuration for this repository!",
					HttpStatus.CONFLICT);
		}

		// Init database config
		GitConfiguration savedConfig = null;

		try {
			// Create configuration object + check if data valid
			GitConfiguration config = grabber.createConfigurationForRepo(repoName, repoOwner, repoService, botUsername,
					botPassword, botEmail, botToken, analysisService, analysisServiceProjectKey, maxAmountRequests,
					projectRootFolder);
			// Try to save configuration to database
			try {
				savedConfig = repo.save(config);
			} catch (Exception e) {
				// Print exception and abort if database error occurs
				e.printStackTrace();
				return new ResponseEntity<String>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
			}

			// Delete local folder for config if exists (if database was resetted)
			if (new File(botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId()).exists()) {
				FileUtils.deleteDirectory(
						new File(botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId()));
			}

			// Create new local folder for the fork
			File dir = new File(botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId());
			dir.mkdir();
			// Create the fork on the filehoster bot account
			grabber.createFork(savedConfig);
			// Clone fork + add remote of origin repository
			gitController.initLocalWorkspace(savedConfig);

			// Fetch target-Repository-Data and check bot password
			gitController.fetchRemote(savedConfig);
			String newBranch = "testCredentialsFor_" + savedConfig.getBotName();
			gitController.createBranch(savedConfig, "master", newBranch);
			gitController.pushChanges(savedConfig, "Test bot password");

			return new ResponseEntity<GitConfiguration>(config, HttpStatus.CREATED);
		} catch (Exception e) {
			// If error occured after config was created
			try {
				// Try to delete configuration if created
				if (savedConfig != null) {
					repo.delete(savedConfig);
				}
				
				// Try to delete local folder
				File forkFolder = new File(
						botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId());
				FileUtils.deleteDirectory(forkFolder);

				// Try to delete Repo
				grabber.deleteRepository(savedConfig);
			} catch (Exception t) {
				t.printStackTrace();
			}

			e.printStackTrace();
			return new ResponseEntity<String>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * This method removes a configuration with a specific ID from the database.
	 * 
	 * @param repoName
	 * @param repoOwner
	 * @param repoService
	 * @return {feedbackString}
	 */
	@RequestMapping(value = "/deleteConfig", method = RequestMethod.DELETE, produces = "application/json")
	@ApiOperation(value = "Delete Git-Konfiguration")
	public ResponseEntity<?> deleteConfig(
			@RequestParam(value = "configurationId", required = true) Long configurationId) {
		// Check if configuration exists
		Optional<GitConfiguration> existsConfig = repo.getByID(configurationId);
		String userFeedback = "";
		// If it does
		if (existsConfig.isPresent()) {
			// Delete configuration from the database
			try {
				repo.delete(existsConfig.get());
				userFeedback = userFeedback.concat("Configuration deleted from database!");
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<String>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			// Delete repository from the filehoster bot account
			try {
				grabber.deleteRepository(existsConfig.get());
			} catch (Exception e) {
				userFeedback = userFeedback
						.concat(" Could not delete repository on " + existsConfig.get().getRepoService() + "!");
			}
			// Delete local folder
			try {
				File forkFolder = new File(
						botConfig.getBotRefactoringDirectory() + existsConfig.get().getConfigurationId());
				FileUtils.deleteDirectory(forkFolder);
			} catch (Exception e) {
				userFeedback = userFeedback.concat(" Could not delete local folder '"
						+ existsConfig.get().getConfigurationId() + "' of the configuration!");
			}
			// Return feedback to user
			return new ResponseEntity<String>(userFeedback, HttpStatus.OK);
		} else {
			return new ResponseEntity<String>("Configuration with given ID does not exist!", HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * This method returns all configurations from the database
	 * 
	 * @return allConfigs
	 */
	@RequestMapping(value = "/getAllConfigs", method = RequestMethod.GET, produces = "application/json")
	@ApiOperation(value = "Get all Git-Configurations")
	public ResponseEntity<?> getAllConfigs() {
		Iterable<GitConfiguration> allConfigs = repo.findAll();
		return new ResponseEntity<Iterable<GitConfiguration>>(allConfigs, HttpStatus.OK);
	}
}
