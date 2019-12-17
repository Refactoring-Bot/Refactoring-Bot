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
				SonarIssue issue3 = createSonarIssue("Remove this unused method parameter \"request\".", locationMsgs, "yesterday");
				SonarIssue issue4 = createSonarIssue("Remove this unused method parameter \"request\".", locationMsgs, "today");

				//settings für Liste die geändert wird
				List<SonarIssue> sonarIssues = new ArrayList<>();
				sonarIssues.add(issue);
				sonarIssues.add(issue2);

				//SonarQubeIssues1
				SonarQubeIssues issues = new SonarQubeIssues();
				issues.setIssues(sonarIssues);

				list.add(issues);

				//SonarQubeIssues2
				List<SonarIssue> sonarIssues2 = new ArrayList<>();
				sonarIssues2.add(issue3);
				sonarIssues2.add(issue4);
				SonarQubeIssues issues2 = new SonarQubeIssues();
				issues2.setIssues(sonarIssues2);

				list.add(issues2);

				//controll List
				List<SonarIssue> sonarIssues3 = new ArrayList<>();
				sonarIssues3.add(issue2);
				sonarIssues3.add(issue);

				SonarQubeIssues issues22 = new SonarQubeIssues();
				issues22.setIssues(sonarIssues3);

				list2.add(issues22);

				List<SonarIssue> sonarIssues23 = new ArrayList<>();
				sonarIssues23.add(issue4);
				sonarIssues23.add(issue3);
				SonarQubeIssues issues23 = new SonarQubeIssues();
				issues23.setIssues(sonarIssues23);
				list2.add(issues23);

				System.out.println("List1.1: " + list.get(0).getIssues().get(0).getCreationDate() + ", " + list.get(0).getIssues().get(1).getCreationDate());
				System.out.println("List2.1: " + list2.get(0).getIssues().get(0).getCreationDate() + ", " + list2.get(0).getIssues().get(1).getCreationDate());

				System.out.println();

				System.out.println("List1.2: " + list.get(1).getIssues().get(0).getCreationDate() + ", " + list.get(1).getIssues().get(1).getCreationDate());
				System.out.println("List2.2: " + list2.get(1).getIssues().get(0).getCreationDate() + ", " + list2.get(1).getIssues().get(1).getCreationDate());

				// act
				ApiGrabber grabber = new ApiGrabber();
				list = grabber.codeSmellPrioritization(list);

				System.out.println("act");

				System.out.println("List1.1: " + list.get(0).getIssues().get(0).getCreationDate() + ", " + list.get(0).getIssues().get(1).getCreationDate());
				System.out.println("List2.1: " + list2.get(0).getIssues().get(0).getCreationDate() + ", " + list2.get(0).getIssues().get(1).getCreationDate());

				System.out.println();

				System.out.println("List1.2: " + list.get(1).getIssues().get(0).getCreationDate() + ", " + list.get(1).getIssues().get(1).getCreationDate());
				System.out.println("List2.2: " + list2.get(1).getIssues().get(0).getCreationDate() + ", " + list2.get(1).getIssues().get(1).getCreationDate());

				// assert
				assertThat(list2.get(0).getIssues()).isEqualTo(list.get(0).getIssues());
				assertThat(list2.get(1).getIssues()).isEqualTo(list.get(1).getIssues());
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
