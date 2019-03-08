package de.refactoringbot.api.sonarqube;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.configuration.GitConfigurationDTO;
import de.refactoringbot.model.exceptions.SonarQubeAPIException;
import de.refactoringbot.model.sonarqube.SonarQubeIssues;

/**
 * This class gets all kinds of data from SonarCube.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class SonarQubeDataGrabber {

	private static final String USER_AGENT = "Mozilla/5.0";

	private static final Logger logger = LoggerFactory.getLogger(SonarQubeDataGrabber.class);

	/**
	 * This method gets all SonarCubeIssues of a Project.
	 * 
	 * @param sonarQubeProjectKey
	 * @return allIssues
	 * @throws SonarQubeAPIException
	 */
	public List<SonarQubeIssues> getIssues(GitConfiguration gitConfig) throws SonarQubeAPIException, URISyntaxException {
		int page = 1;

		List<SonarQubeIssues> issues = new ArrayList<>();

		while (page < 500) {
			// Build URI
			UriComponentsBuilder apiUriBuilder = createUriBuilder(gitConfig.getAnalysisServiceApiLink(), "/issues/search");

			apiUriBuilder.queryParam("componentRoots", gitConfig.getAnalysisServiceProjectKey());
			apiUriBuilder.queryParam("statuses", "OPEN,REOPENED");
			apiUriBuilder.queryParam("ps", 500);
			apiUriBuilder.queryParam("p", page);

			URI sonarQubeURI = apiUriBuilder.build().encode().toUri();

			// Create REST-Template
			RestTemplate rest = new RestTemplate();
			// Build Header
			HttpHeaders headers = new HttpHeaders();
			headers.set("User-Agent", USER_AGENT);
			HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

			// Send request
			try {
				issues.add(rest.exchange(sonarQubeURI, HttpMethod.GET, entity, SonarQubeIssues.class).getBody());
				page++;
			} catch (RestClientException e) {
				if (page == 1) {
					logger.error(e.getMessage(), e);
					throw new SonarQubeAPIException("Could not access SonarCube API!", e);
				}

				break;

			}

		}

		return issues;

	}

	/**
	 * This method checks if a project with the given project key exists on
	 * SonarQube/SonarCloud.
	 * 
	 * @param analysisServiceProjectKey
	 * @throws SonarQubeAPIException
	 */
	public void checkSonarData(GitConfigurationDTO configuration) throws SonarQubeAPIException, URISyntaxException {
		// Build URI
		UriComponentsBuilder apiUriBuilder = createUriBuilder(configuration.getAnalysisServiceApiLink(), "/components/show");

		apiUriBuilder.queryParam("component", configuration.getAnalysisServiceProjectKey());

		URI sonarQubeURI = apiUriBuilder.build().encode().toUri();

		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Build Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		// Send request
		try {
			rest.exchange(sonarQubeURI, HttpMethod.GET, entity, SonarQubeIssues.class).getBody();
		} catch (RestClientException e) {
			logger.error(e.getMessage(), e);
			throw new SonarQubeAPIException("Project with given project key does not exist on SonarQube!", e);
		}
	}

	/**
	 * Attempts to instantiate a URI object using the specified API link. If
	 * successful, it creates an UriComponentsBuilder with the created URI and
	 * returns it.
	 * 
	 * @param link
	 * @param apiEntryPoint
	 * @return apiUriBuilder
	 * @throws URISyntaxException
	 */
	private UriComponentsBuilder createUriBuilder(String link, String apiEntryPoint) throws URISyntaxException {
		URI result = null;
		UriComponentsBuilder apiUriBuilder = null;
		try {
			result = new URI(link);
			apiUriBuilder = UriComponentsBuilder.newInstance().scheme(result.getScheme()).host(result.getHost()).port(result.getPort())
					.path(result.getPath() + apiEntryPoint);
			return apiUriBuilder;
		} catch (URISyntaxException u) {
			logger.error(u.getMessage(), u);
			throw new URISyntaxException("Could not create URI from given API link!", u.getMessage());
		}
	}

}
