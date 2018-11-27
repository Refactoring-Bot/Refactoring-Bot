package de.BA.refactoringBot.rest;

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

import de.BA.refactoringBot.api.main.ApiGrabber;
import de.BA.refactoringBot.configuration.BotConfiguration;
import de.BA.refactoringBot.controller.github.GithubObjectTranslator;
import de.BA.refactoringBot.controller.main.GitController;
import de.BA.refactoringBot.model.configuration.ConfigurationRepository;
import de.BA.refactoringBot.model.configuration.GitConfiguration;
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
			@RequestParam(value = "ownerName", required = true, defaultValue = "TimoPfaff") String repoOwner,
			@RequestParam(value = "ProjectRootFolder", required = true, defaultValue = "Calculator") String projectRootFolder,
			@RequestParam(value = "botUsername", required = true) String botUsername,
			@RequestParam(value = "botPassword", required = true) String botPassword,
			@RequestParam(value = "botToken", required = true) String botToken,
			@RequestParam(value = "analysisService", required = false) String analysisService,
			@RequestParam(value = "analysisServiceProjectKey", required = false) String analysisServiceProjectKey,
			@RequestParam(value = "maxAmountRequests", required = true, defaultValue = "5") Integer maxAmountRequests) {
		// Check if repository already exists in another configuration
		Optional<GitConfiguration> existsConfig = repo.getConfigByName(repoName, repoOwner);
		// If it does
		if (existsConfig.isPresent()) {
			return new ResponseEntity<String>("There is already an configuration for this repository!",
					HttpStatus.CONFLICT);
		}

		try {
			// Create configuration object + check if data valid
			GitConfiguration config = grabber.createConfigurationForRepo(repoName, repoOwner, repoService, botUsername,
					botPassword, botToken, analysisService, analysisServiceProjectKey, maxAmountRequests,
					projectRootFolder);
			// Save configuration to database
			GitConfiguration savedConfig = repo.save(config);
			// Create local folder for the fork
			File dir = new File(botConfig.getBotRefactoringDirectory() + savedConfig.getConfigurationId());
			dir.mkdir();
			// Create the fork on the filehoster bot account
			grabber.createFork(savedConfig);
			// Clone fork + add remote of origin repository
			gitController.initLocalWorkspace(savedConfig);

			return new ResponseEntity<GitConfiguration>(config, HttpStatus.CREATED);
		} catch (Exception e) {
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
		// If it does
		if (existsConfig.isPresent()) {
			try {
				// Delete repository from the filehoster bot account
				grabber.deleteRepository(existsConfig.get());
				// Delete configuration from the database
				repo.delete(existsConfig.get());
				// Delete local folder
				File forkFolder = new File(
						botConfig.getBotRefactoringDirectory() + existsConfig.get().getConfigurationId());
				FileUtils.deleteDirectory(forkFolder);

				return new ResponseEntity<String>("Configuration deleted!", HttpStatus.OK);
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} else {
			return new ResponseEntity<String>("Configuration with given ID does not exist!",
					HttpStatus.NOT_FOUND);
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
