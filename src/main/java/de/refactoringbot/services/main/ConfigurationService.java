package de.refactoringbot.services.main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import de.refactoringbot.api.main.ApiGrabber;
import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.configuration.ConfigurationRepository;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.DatabaseConnectionException;
import de.refactoringbot.model.exceptions.GitHubAPIException;
import de.refactoringbot.services.github.GithubObjectTranslator;
import javassist.NotFoundException;

/**
 * This class contains functions regarding the configuration of the bots.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class ConfigurationService {

	@Autowired
	ConfigurationRepository repo;
	@Autowired
	ApiGrabber grabber;
	@Autowired
	GithubObjectTranslator translator;
	@Autowired
	BotConfiguration botConfig;
	@Autowired
	GitService gitService;
	@Autowired
	BotService botService;

	private static final Logger logger = LoggerFactory.getLogger(ConfigurationService.class);

	/**
	 * This method checks if a configuration exists and returns it if possible.
	 * 
	 * @param configurationId
	 * @return savedConfig
	 * @throws Exception
	 */
	public GitConfiguration checkConfigurationExistance(Long configurationId)
			throws DatabaseConnectionException, NotFoundException {

		Optional<GitConfiguration> existsConfig;
		GitConfiguration savedConfig = null;

		try {
			// Try to get the Git-Configuration with the given ID
			existsConfig = repo.getByID(configurationId);
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			throw new DatabaseConnectionException("Connection with database failed!");
		}

		// Read configuration if possible
		if (existsConfig.isPresent()) {
			savedConfig = existsConfig.get();
		} else {
			throw new NotFoundException("Configuration with given ID does not exist in the database!");
		}

		return savedConfig;
	}

	/**
	 * This method returns all configurations stored inside the database.
	 * 
	 * @return allConfigs
	 * @throws DatabaseConnectionException
	 */
	public Iterable<GitConfiguration> getAllConfigurations() throws DatabaseConnectionException {
		Iterable<GitConfiguration> allConfigs;
		try {
			allConfigs = repo.findAll();
			return allConfigs;
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			throw new DatabaseConnectionException("Connection with database failed!");
		}
	}

	/**
	 * This method creates an initial Configuration with the Repository Data. It
	 * saves it to the database to generate an Database-ID and then returns created
	 * configuration for further processing.
	 * 
	 * @param newConfiguration
	 * @return createdConfig
	 * @throws Exception
	 */
	public GitConfiguration createInitialConfiguration(GitConfigurationDTO newConfiguration) throws Exception {
		// Create configuration
		GitConfiguration createdConfig = grabber.createConfigurationForRepo(newConfiguration);

		try {
			// Try to save the new configuration
			createdConfig = repo.save(createdConfig);
		} catch (Exception e) {
			// Print exception and abort if database error occurs
			logger.error(e.getMessage(), e);
			throw new DatabaseConnectionException("Connection with database failed!");
		}

		return createdConfig;
	}

	/**
	 * This method reverts the configuration process if something fails during the
	 * finalization of the configuration.
	 * 
	 * @param createdConfig
	 * @throws Exception
	 */
	public void revertConfigurationCreation(GitConfiguration createdConfig) throws Exception {
		// Try to delete configuration if created
		if (createdConfig != null) {
			repo.delete(createdConfig);
			// Try to delete local folder
			File forkFolder = new File(botConfig.getBotRefactoringDirectory() + createdConfig.getConfigurationId());
			FileUtils.deleteDirectory(forkFolder);

			// Try to delete Repo
			grabber.deleteRepository(createdConfig);
		}
	}

	/**
	 * This method finalizes the configuration process. Here we initiate our local
	 * workspace for our configuration, create a fork on our filehosting service and
	 * pull the fork. Also we create a remote of the original repository and fetch
	 * its data initially. At last we create paths to our config workspace and
	 * update our config in the db with them.
	 * 
	 * @param config
	 * @return savedConfig
	 * @throws Exception
	 */
	public GitConfiguration finalizeGitConfiguration(GitConfiguration config) throws Exception {
		// Delete local folder for config if exists (if database was resetted)
		if (new File(botConfig.getBotRefactoringDirectory() + config.getConfigurationId()).exists()) {
			FileUtils.deleteDirectory(new File(botConfig.getBotRefactoringDirectory() + config.getConfigurationId()));
		}

		// Create new local folder for the fork
		File dir = new File(botConfig.getBotRefactoringDirectory() + config.getConfigurationId());
		dir.mkdir();
		// Create the fork on the filehoster bot account
		grabber.createFork(config);
		// Clone fork + add remote of origin repository
		gitService.initLocalWorkspace(config);

		// Add repo path and src-folder path to config
		config.setRepoFolder(
				Paths.get(botConfig.getBotRefactoringDirectory() + config.getConfigurationId()).toString());
		config = repo.save(config);

		// Fetch target-Repository-Data
		gitService.fetchRemote(config);

		return config;
	}

	/**
	 * This method deletes a configuration from the database.
	 * 
	 * @param config
	 * @return userFeedback
	 * @throws DatabaseConnectionException
	 */
	public ResponseEntity<?> deleteConfiguration(GitConfiguration config) {
		// Init feedback
		String userFeedback = null;

		// Delete configuration from the database
		try {
			repo.delete(config);
			userFeedback = "Configuration deleted from database!";
		} catch (Exception d) {
			logger.error(d.getMessage(), d);
			return new ResponseEntity<>(d.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
		// Delete repository from the filehoster bot account
		try {
			deleteConfigurationRepo(config);
		} catch (GitHubAPIException e) {
			logger.error(e.getMessage(), e);
			userFeedback = userFeedback.concat(e.getMessage());
		}
		// Delete local folder
		try {
			deleteConfigurationFolder(config);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			userFeedback = userFeedback.concat(e.getMessage());
		}
		// Return feedback to user
		return new ResponseEntity<>(userFeedback, HttpStatus.OK);
	}

	/**
	 * This method deletes the configuration repository/fork from the filehosting
	 * servers.
	 * 
	 * @param config
	 * @throws DatabaseConnectionException
	 */
	public void deleteConfigurationRepo(GitConfiguration config) throws GitHubAPIException {
		// Delete configuration from the database
		try {
			grabber.deleteRepository(config);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new GitHubAPIException(" Could not delete repository on " + config.getRepoService() + "!");
		}
	}

	/**
	 * This method deletes the configuration folder where the repository files were
	 * stored.
	 * 
	 * @param config
	 * @throws DatabaseConnectionException
	 */
	public void deleteConfigurationFolder(GitConfiguration config) throws IOException {
		try {
			File forkFolder = new File(botConfig.getBotRefactoringDirectory() + config.getConfigurationId());
			FileUtils.deleteDirectory(forkFolder);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new IOException(
					" Could not delete local folder '" + config.getConfigurationId() + "' of the configuration!");
		}
	}
}
