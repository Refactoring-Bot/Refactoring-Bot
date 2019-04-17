package de.refactoringBot.api.sonarQube;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.refactoringBot.model.sonarQube.SonarQubeIssues;

/**
 * This class gets all kinds of data from SonarCube.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class SonarQubeDataGrabber {

	private final String USER_AGENT = "Mozilla/5.0";

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(SonarQubeDataGrabber.class);

	/**
	 * This method gets all SonarCubeIssues of a Project.
	 * 
	 * @param sonarQubeProjectKey
	 * @return allIssues
	 * @throws Exception
	 */
	public SonarQubeIssues getIssues(String sonarQubeProjectKey) throws Exception {
		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance();
		//apiUriBuilder.scheme("https").host("sonarcloud.io");
		apiUriBuilder.scheme("http").host("localhost").port(9000);
		apiUriBuilder.path("api/issues/search");

		apiUriBuilder.queryParam("componentRoots", sonarQubeProjectKey);
		apiUriBuilder.queryParam("statuses", "OPEN,REOPENED");
		apiUriBuilder.queryParam("ps", 500);
		apiUriBuilder.queryParam("p",1);
		apiUriBuilder.queryParam("languages", "java");

		URI sonarQubeURI = apiUriBuilder.build().encode().toUri();

		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Build Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		// Send request
		try {
			return rest.exchange(sonarQubeURI, HttpMethod.GET, entity, SonarQubeIssues.class).getBody();
		} catch (RestClientException e) {
			logger.error(e.getMessage(), e);
			throw new Exception("Could not access SonarCube API!");
		}
	}

	/**
	 * This method checks if a project with the given project key exists on
	 * SonarQube/SonarCloud.
	 * 
	 * @param analysisServiceProjectKey
	 * @throws Exception
	 */
	public void checkSonarData(String analysisServiceProjectKey) throws Exception {
		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance();
		//apiUriBuilder.scheme("https").host("sonarcloud.io");
		apiUriBuilder.scheme("http").host("localhost").port(9000);
		apiUriBuilder.path("api/components/show");

		apiUriBuilder.queryParam("component", analysisServiceProjectKey);

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
			throw new Exception("Project with given project key does not exist on SonarQube!");
		}
	}
}
