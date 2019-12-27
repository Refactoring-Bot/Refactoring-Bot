package de.refactoringbot;

import de.refactoringbot.api.main.ApiGrabber;
import de.refactoringbot.model.sonarqube.SonarIssue;
import de.refactoringbot.model.sonarqube.SonarQubeIssues;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
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

				SonarIssue issue = createSonarIssue("A", "2018-11-11T11:15:22+0100", 0);
				SonarIssue issue2 = createSonarIssue("b", "2018-11-11T11:15:23+0100", 0);
				SonarIssue issue3 = createSonarIssue("v", "2018-11-11T11:16:23+0100", 0);
				SonarIssue issue4 = createSonarIssue("f", "2018-11-11T12:16:23+0100", 0);
				SonarIssue issue5 = createSonarIssue("d", "2018-11-12T12:16:23+0100", 0);
				SonarIssue issue6 = createSonarIssue("h", "2018-12-12T12:16:23+0100", 0);
				SonarIssue issue7 = createSonarIssue("hi", "2019-12-12T12:16:23+0100", 0);

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
				Collections.reverse(list2.get(0).getIssues());

				for (int i = 0; i < sonarIssues.size(); i++){
						System.out.println("sorted list: " + sonarIssues.get(i).getKey() + " controll list: " + sonarIssues2.get(i).getKey());
						System.out.println("sorted list: " + sonarIssues.get(i).getCreationDate() + " controll list: " + sonarIssues2.get(i).getCreationDate());
						assertThat(list2.get(0).getIssues().get(i).getCreationDate()).isEqualTo(list.get(0).getIssues().get(i).getCreationDate());
				}

				// assert
				assertThat(list2.get(0).getIssues()).isEqualTo(list.get(0).getIssues());
		}

		@Test
		public void testPrioritization2(){
				// arrange
				List<String> locationMsgs = new ArrayList<>();
				List<SonarQubeIssues> list = new ArrayList<>();
				List<SonarQubeIssues> list2 = new ArrayList<>();
				locationMsgs.add("Remove this unused method parameter request\".");

				SonarIssue issue = createSonarIssue("A", "2018-11-11T11:15:22+0100", 1);
				SonarIssue issue2 = createSonarIssue("b", "2018-11-11T11:15:23+0100", 5);
				SonarIssue issue3 = createSonarIssue("v", "2018-11-11T11:16:23+0100", 5);
				SonarIssue issue4 = createSonarIssue("f", "2018-11-11T12:16:23+0100", 7);
				SonarIssue issue5 = createSonarIssue("d", "2018-11-12T12:16:23+0100", 7);
				SonarIssue issue6 = createSonarIssue("h", "2019-12-12T12:16:23+0100", 8);
				SonarIssue issue7 = createSonarIssue("hi", "2019-12-12T12:16:23+0100", 9);

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
				sonarIssues2.add(issue7);
				sonarIssues2.add(issue6);
				sonarIssues2.add(issue5);
				sonarIssues2.add(issue4);
				sonarIssues2.add(issue3);
				sonarIssues2.add(issue2);
				sonarIssues2.add(issue);

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
				//Collections.reverse(list2.get(0).getIssues());

				for (int i = 0; i < sonarIssues.size(); i++){
						System.out.println("sorted list: " + sonarIssues.get(i).getKey() + " controll list: " + sonarIssues2.get(i).getKey());
						System.out.println("sorted list: " + sonarIssues.get(i).getCreationDate() + " controll list: " + sonarIssues2.get(i).getCreationDate());
						assertThat(list2.get(0).getIssues().get(i).getCreationDate()).isEqualTo(list.get(0).getIssues().get(i).getCreationDate());
				}

				// assert
				assertThat(list2.get(0).getIssues()).isEqualTo(list.get(0).getIssues());
		}

		private SonarIssue createSonarIssue(String key, String creationDate, int count) {
				SonarIssue issue = new SonarIssue();
				issue.setCreationDate(creationDate);
				issue.setKey(key);
				issue.setCountChanges(count);

				return issue;
		}
}
