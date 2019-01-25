package de.refactoringbot.api.wit;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import de.refactoringbot.configuration.BotConfiguration;
import de.refactoringbot.model.exceptions.WitAPIException;
import de.refactoringbot.model.wit.WitObject;

/**
 * This class communicates with the wit.ai REST-Api to exchange data.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class WitDataGrabber {

	@Autowired
	BotConfiguration botConfig;

	private static final String USER_AGENT = "Mozilla/5.0";

	/**
	 * This method communicates with the wit-API. It returns an WitObject with the
	 * input of an message.
	 * 
	 * @param message
	 * @return witObject
	 * @throws RestClientException
	 */
	public WitObject getWitObjectFromMessage(String message) throws WitAPIException {
		// Build URI
		UriComponentsBuilder apiUriBuilder = UriComponentsBuilder.newInstance().scheme("https").host("api.wit.ai")
				.path("/message");

		apiUriBuilder.queryParam("v", "20190122");
		apiUriBuilder.queryParam("q", message);

		URI witUri = apiUriBuilder.build().encode().toUri();

		// Create REST-Template
		RestTemplate rest = new RestTemplate();
		// Baue Header
		HttpHeaders headers = new HttpHeaders();
		headers.set("User-Agent", USER_AGENT);
		headers.set("Authorization", "Bearer " + botConfig.getWitClientToken());
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);

		try {
			// Send request to the Wit-API
			return rest.exchange(witUri, HttpMethod.GET, entity, WitObject.class).getBody();
		} catch (RestClientException r) {
			throw new WitAPIException("Could not exchange data with wit.ai!");
		}

	}
}
