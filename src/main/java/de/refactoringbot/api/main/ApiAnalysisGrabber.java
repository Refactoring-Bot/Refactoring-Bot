package de.refactoringbot.api.main;

import de.refactoringbot.api.sonarqube.SonarQubeDataGrabber;
import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.AnalysisProvider;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.SonarQubeAPIException;
import de.refactoringbot.model.sonarqube.SonarQubeIssues;
import de.refactoringbot.services.sonarqube.SonarQubeObjectTranslator;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class ApiAnalysisGrabber {
    @Autowired
    SonarQubeDataGrabber sonarQubeGrabber;
    @Autowired
    SonarQubeObjectTranslator sonarQubeTranslator;

    /**
     * This method gets all issues of a Project from a analysis service.
     *
     * @param gitConfig
     * @return botIssues
     * @throws Exception
     */
    public List<BotIssue> getAnalysisServiceIssues(GitConfiguration gitConfig) throws Exception {
        // Pick service
        switch (gitConfig.getAnalysisService()) {
            case sonarqube:
                // Get issues and translate them
                List<SonarQubeIssues> issues = sonarQubeGrabber.getIssues(gitConfig);
                List<BotIssue> botIssues = new ArrayList<>();
                for (SonarQubeIssues i : issues) {
                    botIssues.addAll(sonarQubeTranslator.translateSonarIssue(i, gitConfig));
                }
                return botIssues;
            default:
                throw new UnsupportedOperationException(
                        "Analysis-Service '" + gitConfig.getAnalysisService() + "' is not supported!");
        }
    }

    /**
     * This method returns the absolute path of a anaylsis service issue. This is
     * only necessary for analysis services that only return relative paths for
     * their issues (e.g. SonarQube). Other anaylsis services should just return the
     * input path in their case-section.
     *
     * @param gitConfig
     * @param relativePath
     * @return absoluteFilePath
     * @throws IOException
     */
    public String getAnalysisServiceAbsoluteIssuePath(GitConfiguration gitConfig, String relativePath)
            throws IOException {
        // Pick service
        switch (gitConfig.getAnalysisService()) {
            case sonarqube:
                return sonarQubeTranslator.buildIssuePath(gitConfig, relativePath);
            default:
                return null;
        }
    }

    /**
     * This method checks the analysis service data.
     *
     * @param analysisService
     * @param analysisServiceProjectKey
     * @throws SonarQubeAPIException
     * @throws URISyntaxException
     */
    protected void checkAnalysisService(GitConfigurationDTO configuration) throws SonarQubeAPIException, URISyntaxException {
        // Check if input exists
        if (configuration.getAnalysisService() == null || configuration.getAnalysisServiceProjectKey() == null
                || configuration.getAnalysisServiceApiLink() == null) {
            return;
        }
        // Pick service
        if (configuration.getAnalysisService().equals(AnalysisProvider.sonarqube)) {
            sonarQubeGrabber.checkSonarData(configuration);
        }
    }
}
