package de.refactoringbot.rest;

import de.refactoringbot.api.github.GithubDataGrabber;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.model.github.pullrequest.GithubPullRequest;
import de.refactoringbot.model.github.pullrequest.GithubPullRequests;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.refactoringbot.services.main.ConfigurationService;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import io.swagger.annotations.ApiOperation;
import javassist.NotFoundException;

import java.io.IOException;
import java.net.URISyntaxException;

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
	ConfigurationService configService;
	@Autowired
	ModelMapper modelMapper;
	@Autowired
	GithubDataGrabber grabber;

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

	/**
	 * This method creates an git configuration with the user inputs.
	 * 
	 * @param newConfiguration
	 * @return
	 */

	@PostMapping(consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Create Git-Konfiguration")
	public ResponseEntity<Object> add(@RequestBody GitConfigurationDTO newConfiguration) {
		GitConfiguration savedConfig = null;
		try {
			// Create initial configuration
			savedConfig = configService.createInitialConfiguration(newConfiguration);
			// Finalize configuration process
			savedConfig = configService.finalizeGitConfiguration(savedConfig);
			return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
		} catch (DatabaseConnectionException d) {
			// Print exception and abort if database error occurs
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			// If error occured after config was created
			try {
				// Try to delete configuration if created
				configService.revertConfigurationCreation(savedConfig);
			} catch (Exception t) {
				logger.error(t.getMessage(), t);
			}

			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	@PutMapping(path = "/{configurationId}", consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Update Git-Configuration with configuration id")
	public ResponseEntity<?> update(@RequestBody GitConfigurationDTO newConfiguration,
			@PathVariable(name = "configurationId") Long configurationId) {
		GitConfiguration savedConfig = null;
		try {
			savedConfig = configService.checkConfigurationExistance(configurationId);
		} catch (DatabaseConnectionException d) {
			// Print exception and abort if database error occurs
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException n) {
			return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
		}

		// Init database config
		modelMapper.map(newConfiguration, savedConfig);

		// Finalize configuration process
		try {
			savedConfig = configService.finalizeGitConfiguration(savedConfig);
			return new ResponseEntity<>(savedConfig, HttpStatus.CREATED);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		}
	}

	/**
	 * This method removes a configuration with a specific ID from the database.
	 * 
	 * @param configurationId
	 * @return {feedbackString}
	 */

	@DeleteMapping(path = "/{configurationId}", produces = "application/json")
	@ApiOperation(value = "Delete Git-Configuration with configuration id")
	public ResponseEntity<?> deleteConfig(@PathVariable("configurationId") Long configurationId) {
		// Check if configuration exists
		GitConfiguration config = null;
		try {
			config = configService.checkConfigurationExistance(configurationId);
		} catch (DatabaseConnectionException d) {
			// Print exception and abort if database error occurs
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException n) {
			return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
		}
		
		// Remove Configuration and respond
		return configService.deleteConfiguration(config);
	}

	/**
	 * This method returns all configurations from the database
	 * 
	 * @return allConfigs
	 */
	@GetMapping(produces = "application/json")
	@ApiOperation(value = "Get all Git-Configurations")
	public ResponseEntity<?> getAllConfigs() {
		try {
			Iterable<GitConfiguration> allConfigs = configService.getAllConfigurations();
			return new ResponseEntity<>(allConfigs, HttpStatus.OK);
		} catch (DatabaseConnectionException e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} 
	}

	/**
	 * This method returns a configuration with a specific ID.
	 * 
	 * @param configurationId
	 * @return config
	 */
	@GetMapping(path = "/{configurationId}", produces = "application/json")
	@ApiOperation(value = "Get Git-Configuration with configuration id")
	public ResponseEntity<?> getConfigById(@PathVariable("configurationId") Long configurationId) {
		// Check if configuration exists
		GitConfiguration config = null;
		try {
			config = configService.checkConfigurationExistance(configurationId);
			return new ResponseEntity<>(config, HttpStatus.OK);
		} catch (DatabaseConnectionException d) {
			// Print exception and abort if database error occurs
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException n) {
			return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
		}
	}

	/**
	 * This method returns a list of all open pullrequest.
	 *
	 * @param configurationId
	 * @return config
	 */
	@GetMapping(path = "/{configurationId}/openPullRequests", produces = "application/json")
	@ApiOperation(value = "Get amount of open Pull Requests")
	public ResponseEntity<?> getOpenPullRequests(@PathVariable("configurationId") Long configurationId) throws GitHubAPIException, IOException, URISyntaxException, JSONException {
		// Check if configuration exists
		GitConfiguration config = null;
		try {
			config = configService.checkConfigurationExistance(configurationId);
		} catch (DatabaseConnectionException d) {
			// Print exception and abort if database error occurs
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (NotFoundException n) {
			return new ResponseEntity<>(n.getMessage(), HttpStatus.NOT_FOUND);
		}
		GithubPullRequests json = null;
		json = grabber.getAllPullRequests(config);
		double amountOfOpenPR = 0.0;
		double ratio = 0.0;

		for (GithubPullRequest pullRequest: json.getAllPullRequests() ) {
			if (pullRequest.getState().equals("open") && pullRequest.getUser().getLogin().equals(config.getBotName())){
				amountOfOpenPR++;
			}
		}
		if(json.getAllPullRequests().size() > 0) {
			ratio = amountOfOpenPR / json.getAllPullRequests().size();
		}
		JSONArray openPullRequests = new JSONArray();
		JSONObject openPullRequest = new JSONObject();
		openPullRequest.put("amount", amountOfOpenPR);
		openPullRequest.put("ratio", ratio);
		openPullRequests.put(openPullRequest);

		return new ResponseEntity<>(openPullRequests.toString(), HttpStatus.OK);
	}
}
