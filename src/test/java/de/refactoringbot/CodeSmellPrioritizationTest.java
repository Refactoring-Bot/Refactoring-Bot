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

				SonarIssue issue = createSonarIssue("A", "Remove this unused method parameter \"request\".", locationMsgs, "2018-11-11T11:15:22+0100");
				SonarIssue issue2 = createSonarIssue("b","Remove this unused method parameter \"request\".", locationMsgs, "2018-11-11T11:15:23+0100");
				SonarIssue issue3 = createSonarIssue("v","Remove this unused method parameter \"request\".", locationMsgs, "2018-11-11T11:16:23+0100");
				SonarIssue issue4 = createSonarIssue("f","Remove this unused method parameter \"request\".", locationMsgs, "2018-11-11T12:16:23+0100");
				SonarIssue issue5 = createSonarIssue("d","Remove this unused method parameter \"request\".", locationMsgs, "2018-11-12T12:16:23+0100");
				SonarIssue issue6 = createSonarIssue("h","Remove this unused method parameter \"request\".", locationMsgs, "2018-12-12T12:16:23+0100");
				SonarIssue issue7 = createSonarIssue("hi","Remove this unused method parameter \"request\".", locationMsgs, "2019-12-12T12:16:23+0100");

				//settings für Liste die geändert wird
				List<SonarIssue> sonarIssues = new ArrayList<>();
				sonarIssues.add(issue7);
				sonarIssues.add(issue2);
				sonarIssues.add(issue6);
				sonarIssues.add(issue);
				sonarIssues.add(issue4);
				sonarIssues.add(issue5);
				sonarIssues.add(issue3);

				//SonarQubeIssues1
				SonarQubeIssues issues = new SonarQubeIssues();
				issues.setIssues(sonarIssues);

				list.add(issues);

				//controll list
				List<SonarIssue> sonarIssues2 = new ArrayList<>();
				sonarIssues2.add(issue);
				sonarIssues2.add(issue2);
				sonarIssues2.add(issue3);
				sonarIssues2.add(issue4);
				sonarIssues2.add(issue5);
				sonarIssues2.add(issue6);
				sonarIssues2.add(issue7);

				SonarQubeIssues issues2 = new SonarQubeIssues();
				issues2.setIssues(sonarIssues2);

				list2.add(issues2);

				for (int i = 0; i < sonarIssues.size(); i++){
						System.out.println("unsorted list: " + sonarIssues.get(i).getCreationDate());
				}

				// act
				ApiGrabber grabber = new ApiGrabber();
				list = grabber.codeSmellPrioritization(list);
				sonarIssues = list.get(0).getIssues();

				for (int i = 0; i < sonarIssues.size(); i++){
						System.out.println("sorted list: " + sonarIssues.get(i).getKey() + " controll list: " + sonarIssues2.get(i).getKey());
						System.out.println("sorted list: " + sonarIssues.get(i).getCreationDate() + " controll list: " + sonarIssues2.get(i).getCreationDate());
						assertThat(list2.get(0).getIssues().get(i).getCreationDate()).isEqualTo(list.get(0).getIssues().get(i).getCreationDate());
				}

				// assert
				assertThat(list2.get(0).getIssues()).isEqualTo(list.get(0).getIssues());
		}

		private SonarIssue createSonarIssue(String key, String message, List<String> locationMsgs, String creationDate) {
				SonarIssue issue = new SonarIssue();
				issue.setMessage(message);
				issue.setCreationDate(creationDate);
				issue.setKey(key);

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
