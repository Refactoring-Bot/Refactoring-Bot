package de.refactoringBot.rest;

import java.io.File;
import java.nio.file.Paths;
import java.util.Optional;

import de.refactoringBot.model.configuration.GitConfigurationDTO;
import org.apache.commons.io.FileUtils;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import de.refactoringBot.api.main.ApiGrabber;
import de.refactoringBot.configuration.BotConfiguration;
import de.refactoringBot.controller.github.GithubObjectTranslator;
import de.refactoringBot.controller.main.BotController;
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
@RequestMapping(path = "/configurations")
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
	@Autowired
	BotController botController;
	
	// Logger
    private static final Logger logger = LoggerFactory.getLogger(RefactoringController.class);

	@Autowired
	ModelMapper modelMapper;


	/**
	 * This method creates an git configuration with the user inputs.
	 * 
	 * @param repoName
	 * @param repoOwner
	 * @param repoService
	 * @return
	 */

	@PostMapping(consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Create Git-Konfiguration")
	public ResponseEntity<Object> add(
			@RequestBody GitConfigurationDTO newConfiguration) {
	    GitConfiguration savedConfig = null;
		try {
            savedConfig = grabber.createConfigurationForRepo(newConfiguration);
			try {
				// Try to save the new configuration
				savedConfig = repo.save(savedConfig);
			} catch (Exception e) {
				// Print exception and abort if database error occurs
				logger.error(e.getMessage(), e);
				return new ResponseEntity<>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
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

			// Add repo path and src-folder path to config
			savedConfig.setRepoFolder(
					Paths.get(botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId()).toString());
			savedConfig.setSrcFolder(botController
					.findSrcFolder(botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId()));

			savedConfig = repo.save(savedConfig);

			// Fetch target-Repository-Data and check bot password
			gitController.fetchRemote(savedConfig);
			String newBranch = "testCredentialsFor_" + savedConfig.getBotName();
			gitController.createBranch(savedConfig, "master", newBranch, "upstream");
			gitController.pushChanges(savedConfig, "Test bot password");

			return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
		} catch (Exception e) {
			// If error occured after config was created
			try {
				// Try to delete configuration if created
				if (savedConfig != null) {
					repo.delete(savedConfig);
					// Try to delete local folder
					File forkFolder = new File(
							botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId());
					FileUtils.deleteDirectory(forkFolder);

					// Try to delete Repo
					grabber.deleteRepository(savedConfig);
				}
			} catch (Exception t) {
				logger.error(t.getMessage(), t);
			}

			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/{configurationId}", consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Update Git-Configuration with configuration id")
	public ResponseEntity<?> update(
			@RequestBody GitConfigurationDTO newConfiguration,
			@PathVariable(name = "configurationId") Long configurationId) {
		// Check if configuration exists
		Optional<GitConfiguration> existsConfig;
		try {
			// Try to get the Git-Configuration with the given ID
			existsConfig = repo.getByID(configurationId);
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}


		// unwrap optional
		GitConfiguration savedConfig = existsConfig.get();
		// Init database config
		modelMapper.map(newConfiguration, savedConfig);

		try {

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

			// Add repo path and src-folder path to config
			savedConfig.setRepoFolder(
					Paths.get(botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId()).toString());
			savedConfig.setSrcFolder(botController
					.findSrcFolder(botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId()));
			repo.save(savedConfig);

			// Fetch target-Repository-Data and check bot password
			gitController.fetchRemote(savedConfig);
			String newBranch = "testCredentialsFor_" + savedConfig.getBotName();
			gitController.createBranch(savedConfig, "master", newBranch, "upstream");
			gitController.pushChanges(savedConfig, "Test bot password");

			return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);

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

	@DeleteMapping(path = "/{configurationId}", produces = "application/json")
	@ApiOperation(value = "Delete Git-Configuration with configuration id")
	public ResponseEntity<?> deleteConfig(
			@PathVariable("configurationId") Long configurationId) {
		// Check if configuration exists
		Optional<GitConfiguration> existsConfig;
		try {
			// Try to get the Git-Configuration with the given ID
			existsConfig = repo.getByID(configurationId);
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		String userFeedback = "";
		// If it does
		if (existsConfig.isPresent()) {
			// Delete configuration from the database
			try {
				repo.delete(existsConfig.get());
				userFeedback = userFeedback.concat("Configuration deleted from database!");
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				return new ResponseEntity<>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
			}
			// Delete repository from the filehoster bot account
			try {
				grabber.deleteRepository(existsConfig.get());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				userFeedback = userFeedback
						.concat(" Could not delete repository on " + existsConfig.get().getRepoService() + "!");
			}
			// Delete local folder
			try {
				File forkFolder = new File(
						botConfig.getBotRefactoringDirectory() + existsConfig.get().getConfigurationId());
				FileUtils.deleteDirectory(forkFolder);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				userFeedback = userFeedback.concat(" Could not delete local folder '"
						+ existsConfig.get().getConfigurationId() + "' of the configuration!");
			}
			// Return feedback to user
			return new ResponseEntity<>(userFeedback, HttpStatus.OK);
		} else {
			return new ResponseEntity<>("Configuration with given ID does not exist!", HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * This method returns all configurations from the database
	 * 
	 * @return allConfigs
	 */

	@GetMapping(produces = "application/json")
	@ApiOperation(value = "Get all Git-Configurations")
	public ResponseEntity<?> getAllConfigs() {
		Iterable<GitConfiguration> allConfigs;
		try {
			allConfigs = repo.findAll();
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(allConfigs, HttpStatus.OK);
	}

	@GetMapping(path = "/{configurationId}", produces = "application/json")
	@ApiOperation(value = "Get Git-Configuration with configuration id")
	public ResponseEntity<?> getAllConfigs(
			@PathVariable("configurationId") Long configurationId) {
		Optional<GitConfiguration> existsConfig;
		try {
			// Try to get the Git-Configuration with the given ID
			existsConfig = repo.getByID(configurationId);
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>("Connection with database failed!", HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return new ResponseEntity<>(existsConfig, HttpStatus.OK);
	}
}
