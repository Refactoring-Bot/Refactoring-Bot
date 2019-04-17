package de.refactoringbot.services.sonarqube;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.refactoringbot.model.sonarqube.Flow;
import de.refactoringbot.model.sonarqube.Location;
import de.refactoringbot.model.sonarqube.SonarIssue;

public class SonarQubeObjectTranslatorTest {

	@Test
	public void testGetFirstParameterNameFromRemoveParameterIssue() {
		// arrange
		List<String> locationMsgs = new ArrayList<>();
		locationMsgs.add("Remove this unused method parameter request\".");
		SonarIssue issue = createSonarIssue("Remove this unused method parameter \"request\".", locationMsgs);

		// act
		SonarQubeObjectTranslator translator = new SonarQubeObjectTranslator();
		String paramName = translator.getNameOfFirstUnusedParameterInIssue(issue);

		// assert
		assertThat(paramName).isEqualTo("request");
	}

	/**
	 * Mutiple unused method parameters in the same signature
	 */
	@Test
	public void testGetFirstParameterNameFromCombinedRemoveParameterIssue() {
		// arrange
		List<String> locationMsgs = new ArrayList<>();
		locationMsgs.add("Remove this unused method parameter filter\".");
		locationMsgs.add("Remove this unused method parameter orderBy\".");
		locationMsgs.add("Remove this unused method parameter orderDirection\".");
		SonarIssue issue = createSonarIssue("Remove these unused method parameters.", locationMsgs);

		// act
		SonarQubeObjectTranslator translator = new SonarQubeObjectTranslator();
		String paramName = translator.getNameOfFirstUnusedParameterInIssue(issue);

		// assert
		assertThat(paramName).isEqualTo("filter");
	}

	private SonarIssue createSonarIssue(String message, List<String> locationMsgs) {
		SonarIssue issue = new SonarIssue();
		issue.setMessage(message);

		List<Location> locations = new ArrayList<>();
		for (String msg : locationMsgs) {
			Location location = new Location();
			location.setMsg(msg);
			locations.add(location);

			List<Flow> flows = new ArrayList<>();
			Flow flow = new Flow();
			flow.setLocations(locations);
			flows.add(flow);

			issue.setFlows(flows);
		}

		return issue;
	}

}
