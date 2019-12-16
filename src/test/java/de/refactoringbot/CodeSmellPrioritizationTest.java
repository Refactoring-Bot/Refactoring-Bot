package de.refactoringbot;

import de.refactoringbot.api.main.ApiGrabber;
import de.refactoringbot.api.sonarqube.SonarQubeDataGrabber;
import de.refactoringbot.model.sonarqube.Flow;
import de.refactoringbot.model.sonarqube.Location;
import de.refactoringbot.model.sonarqube.SonarIssue;
import de.refactoringbot.model.sonarqube.SonarQubeIssues;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * this method will test the code-smell prioritization
 */
public class CodeSmellPrioritizationTest {

		@Test
		public void testPrioritization1(){
				// arrange
				List<String> locationMsgs = new ArrayList<>();
				List<SonarQubeIssues> list = new ArrayList<>();
				List<SonarQubeIssues> list2 = new ArrayList<>();
				locationMsgs.add("Remove this unused method parameter request\".");

				SonarIssue issue = createSonarIssue("Remove this unused method parameter \"request\".", locationMsgs, "yesterday");
				SonarIssue issue2 = createSonarIssue("Remove this unused method parameter \"request\".", locationMsgs, "today");

				//settings für Liste die geändert wird
				List<SonarIssue> sonarIssues = new ArrayList<>();
				sonarIssues.add(issue);
				sonarIssues.add(issue2);

				SonarQubeIssues issues = new SonarQubeIssues();
				issues.setIssues(sonarIssues);

				list.add(issues);

				//settings für kontroll Liste
				List<SonarIssue> sonarIssues2 = new ArrayList<>();
				sonarIssues2.add(issue2);
				sonarIssues2.add(issue);

				SonarQubeIssues issues2 = new SonarQubeIssues();
				issues2.setIssues(sonarIssues2);

				// act
				ApiGrabber grabber = new ApiGrabber();
				//list2 = grabber.codeSmellPrioritization(list);
				list2.add(issues2);

				// assert
				assertThat(list2.get(0).getIssues()).isEqualTo(list.get(0).getIssues());
		}

		private SonarIssue createSonarIssue(String message, List<String> locationMsgs, String creationDate) {
				SonarIssue issue = new SonarIssue();
				issue.setMessage(message);
				issue.setCreationDate(creationDate);

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
