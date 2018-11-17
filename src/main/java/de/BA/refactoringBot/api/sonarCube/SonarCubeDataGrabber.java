package de.BA.refactoringBot.api.sonarCube;

import java.net.URI;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.BA.refactoringBot.model.sonarQube.SonarCubeIssues;

/**
 * This class gets all kinds of data from SonarCube.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class SonarCubeDataGrabber {

	private final String USER_AGENT = "Mozilla/5.0";

	/**
	 * This method gets all SonarCubeIssues of a Project.
	 * 
	 * @param sonarCubeProjectKey
	 * @return allIssues
	 * @throws Exception
	 */
	public SonarCubeIssues getIssues(String sonarCubeProjectKey) throws Exception {
		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("sonarcloud.io")
				.path("api/issues/search");

		apiUriBuilder.queryParam("componentRoots", sonarCubeProjectKey);
		apiUriBuilder.queryParam("statuses", "OPEN,REOPENED");

		URI sonarCubeURI = apiUriBuilder.build().encode().toUri();

		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Build Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		// Send request
		try {
			return rest.exchange(sonarCubeURI, HttpMethod.GET, entity, SonarCubeIssues.class).getBody();
		} catch (RestClientException e) {
			throw new Exception("Could not access SonarCube API!");
		}
	}
}
