package de.refactoringbot.api.sonarqube;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.net.URISyntaxException;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import de.refactoringbot.model.configuration.AnalysisProvider;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.SonarQubeAPIException;
import de.refactoringbot.model.sonarqube.SonarQubeIssues;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { SonarQubeDataGrabber.class })
@EnableConfigurationProperties
public class SonarQubeDataGrabberIT {

	@Autowired
	SonarQubeDataGrabber sonarQubeDataGrabber;

	private static final String API_LINK = "https://sonarcloud.io/api";
	private static final String PROJECT_KEY = "Bot-Playground:Bot-Playground";

	@Rule
	public final ExpectedException exception = ExpectedException.none();

	@BeforeClass
	public static void beforeClass() {
		/*
		 * This test gets skipped if it is not executed on the build server. To
		 * successfully run it on a local machine, this method must not be executed.
		 */
		assumeThat(System.getenv("TRAVIS")).isNotNull();
	}
	
	@Test
	public void testCheckSonarData() throws Exception {
		// arrange
		GitConfigurationDTO gitConfig = new GitConfigurationDTO();
		gitConfig.setAnalysisService(AnalysisProvider.sonarqube);
		gitConfig.setAnalysisServiceApiLink(API_LINK);
		gitConfig.setAnalysisServiceProjectKey(PROJECT_KEY);

		// act
		boolean allChecksOkay = sonarQubeDataGrabber.checkSonarData(gitConfig);
		
		// assert
		assertThat(allChecksOkay).isTrue();
	}

	@Test
	public void testCheckSonarDataNotExistingProjectKey() throws Exception {
		exception.expect(SonarQubeAPIException.class);
		
		// arrange
		GitConfigurationDTO gitConfig = new GitConfigurationDTO();
		gitConfig.setAnalysisService(AnalysisProvider.sonarqube);
		gitConfig.setAnalysisServiceApiLink(API_LINK);
		gitConfig.setAnalysisServiceProjectKey("should-not-exist");

		// act
		sonarQubeDataGrabber.checkSonarData(gitConfig);
	}
	
	@Test
	public void testCheckSonarDataWrongAPILink() throws Exception {
		exception.expect(URISyntaxException.class);
		
		// arrange
		GitConfigurationDTO gitConfig = new GitConfigurationDTO();
		gitConfig.setAnalysisService(AnalysisProvider.sonarqube);
		gitConfig.setAnalysisServiceApiLink("definitely not a valid URL");
		gitConfig.setAnalysisServiceProjectKey(PROJECT_KEY);

		// act
		sonarQubeDataGrabber.checkSonarData(gitConfig);
	}
	
	@Test
	public void testGetIssues() throws Exception {
		// arrange
		GitConfiguration gitConfig = new GitConfiguration();
		gitConfig.setAnalysisService(AnalysisProvider.sonarqube);
		gitConfig.setAnalysisServiceApiLink(API_LINK);
		gitConfig.setAnalysisServiceProjectKey(PROJECT_KEY);
		
		// act
		List<SonarQubeIssues> issueBuckets = sonarQubeDataGrabber.getIssues(gitConfig);
		
		// assert
		assertThat(issueBuckets).hasSize(1);
		assertThat(issueBuckets.get(0).getIssues().size()).isBetween(3, 100);
	}

}
