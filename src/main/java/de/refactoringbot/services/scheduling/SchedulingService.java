package de.refactoringbot.services.scheduling;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.configuration.ConfigurationRepository;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.services.main.RefactoringService;

/**
 * This class performs scheduled tasks.
 * 
 * @author Stefan Basaric
 *
 */
@Service
public class SchedulingService {

	@Autowired
	RefactoringService refactoring;
	@Autowired
	BotConfiguration botConfig;
	@Autowired
	ConfigurationRepository repo;
	
	@Value("${scheduling.enable:false}")
	private boolean isSchedulingEnabled;
	
	private static final Logger logger = LoggerFactory.getLogger(SchedulingService.class);

	/**
	 * This method performs a scheduled refactoring on all configurations with a
	 * delay of 1 minute. It only performs refactorings with comments.
	 */
	@Scheduled(fixedDelayString = "${scheduling.delayInMS:10000}")
	public void performCommentRefactorings() {
		
		if(!isSchedulingEnabled) {
			return;
		}
		
		Iterable<GitConfiguration> allConfigs = repo.findAll();
		
		for (GitConfiguration config: allConfigs) {
			try {
				logger.info("Starting scheduled comment refactoring with configuration with the ID: " + config.getConfigurationId());
				refactoring.performRefactoring(config.getConfigurationId(), true);
				logger.info("Successfully finished comment refactoring with configuration with the ID: " + config.getConfigurationId());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		
	}

}
