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
 * This class gets all kinds of data from SonarQube.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class SonarQubeDataGrabber {

	private static final String USER_AGENT = "Mozilla/5.0";

	private static final Logger logger = LoggerFactory.getLogger(SonarQubeDataGrabber.class);

	/**
	 * This method gets all SonarQubeIssues of a Project.
	 * 
	 * @param sonarQubeProjectKey
	 * @return allIssues
	 * @throws SonarQubeAPIException
	 * @throws URISyntaxException
	 */
	public List<SonarQubeIssues> getIssues(GitConfiguration gitConfig)
			throws SonarQubeAPIException, URISyntaxException {
		final int maxPage = 4; // results in a maximum of 4 * 500 = 2000 issues
		final int maxNumberOfIssuesInASingleCall = 500; // maximum allowed by the sonarQube API
		
		List<SonarQubeIssues> issues = new ArrayList<>();
		
		for (int page = 1; page < maxPage; page++) {
			// Build URI
			UriComponentsBuilder apiUriBuilder = createUriBuilder(gitConfig.getAnalysisServiceApiLink(),
					"/issues/search");

			apiUriBuilder.queryParam("componentKeys", gitConfig.getAnalysisServiceProjectKey());
			apiUriBuilder.queryParam("statuses", "OPEN,REOPENED");
			apiUriBuilder.queryParam("ps", maxNumberOfIssuesInASingleCall);
			apiUriBuilder.queryParam("p", page);

			URI sonarQubeURI = apiUriBuilder.build().encode().toUri();

			RestTemplate rest = new RestTemplate();

			HttpHeaders headers = new HttpHeaders();
			headers.set("User-Agent", USER_AGENT);
			HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

			try {
				// Send request
				SonarQubeIssues issueBucket = rest.exchange(sonarQubeURI, HttpMethod.GET, entity, SonarQubeIssues.class)
						.getBody();
				issues.add(issueBucket);
				if (issueBucket.getIssues().size() < maxNumberOfIssuesInASingleCall) {
					break;
				}
			} catch (RestClientException e) {
				if (page == 1) {
					logger.error(e.getMessage(), e);
					throw new SonarQubeAPIException("Could not access SonarQube API!", e);
				}
				break;
			}
		}

		return issues;

	}

	/**
	 * This method checks if a project with the given project key exists on
	 * SonarQube.
	 * 
	 * @param analysisServiceProjectKey
	 * @throws SonarQubeAPIException
	 * @throws URISyntaxException
	 * 
	 * @return true if configured analysis service is valid, throws exception
	 *         otherwise
	 */
	public boolean checkSonarData(GitConfigurationDTO configuration) throws SonarQubeAPIException, URISyntaxException {
		// Build URI
		UriComponentsBuilder apiUriBuilder = null;
		try {
			apiUriBuilder = createUriBuilder(configuration.getAnalysisServiceApiLink(), "/components/show");
		} catch (URISyntaxException e) {
			throw new URISyntaxException(
					"Error checking analysis service data. Could not create URI from given API link! ", e.getMessage());
		}
		apiUriBuilder.queryParam("component", configuration.getAnalysisServiceProjectKey());
		URI sonarQubeURI = apiUriBuilder.build().encode().toUri();

		RestTemplate rest = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		try {
			rest.exchange(sonarQubeURI, HttpMethod.GET, entity, SonarQubeIssues.class).getBody();
		} catch (RestClientException e) {
			throw new SonarQubeAPIException(
					"Error checking analysis service data. Project with given project key might not exist.");
		}

		return true;
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
		result = new URI(link);
		apiUriBuilder = UriComponentsBuilder.newInstance().scheme(result.getScheme()).host(result.getHost())
				.port(result.getPort()).path(result.getPath() + apiEntryPoint);
		return apiUriBuilder;
	}

}
