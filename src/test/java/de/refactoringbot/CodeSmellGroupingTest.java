package de.refactoringbot;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.botissuegroup.BotIssueGroup;
import de.refactoringbot.model.botissuegroup.BotIssueGroupType;
import de.refactoringbot.model.exceptions.BotIssueTypeException;
import de.refactoringbot.refactoring.RefactoringOperations;
import de.refactoringbot.services.main.RefactoringService;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CodeSmellGroupingTest {

		@Test
		public void groupingTest1(){
				RefactoringService service = new RefactoringService();

				List<BotIssue> issues = new ArrayList<>();
				List<BotIssue> issues2 = new ArrayList<>();

				List<BotIssueGroup> groups = new ArrayList<>();
				List<BotIssueGroup> controllList = new ArrayList<>();
				BotIssueGroup group;

				BotIssue botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssue.setFilePath("classA");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssue.setFilePath("classC");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssue.setFilePath("classA");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
				botIssue.setFilePath("classA");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
				botIssue.setFilePath("classA");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REORDER_MODIFIER);
				botIssue.setFilePath("classA");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
				botIssue.setFilePath("classB");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssue.setFilePath("classC");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REORDER_MODIFIER);
				botIssue.setFilePath("classA");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssue.setFilePath("classC");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssue.setFilePath("classD");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssue.setFilePath("classC");
				issues.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssue.setFilePath("classD");
				issues.add(botIssue);

				try {
						groups = service.grouping(issues);
				} catch (BotIssueTypeException e) {
						e.printStackTrace();
				}

				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssue.setFilePath("classA");
				issues2.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssue.setFilePath("classA");
				issues2.add(botIssue);

				try {
						group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						group.addIssues(issues2);
						controllList.add(group);
						//group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						//controllList.add(group);
				} catch (BotIssueTypeException e) {
						e.printStackTrace();
				} finally {
						issues2.clear();
				}

				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssue.setFilePath("classC");
				issues2.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.ADD_OVERRIDE_ANNOTATION);
				botIssue.setFilePath("classC");
				issues2.add(botIssue);

				try {
						group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						group.addIssues(issues2);
						controllList.add(group);
						//group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						//controllList.add(group);
				} catch (BotIssueTypeException e) {
						e.printStackTrace();
				} finally {
						issues2.clear();
				}

				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
				botIssue.setFilePath("classA");
				issues2.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
				botIssue.setFilePath("classA");
				issues2.add(botIssue);

				try {
						group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						group.addIssues(issues2);
						controllList.add(group);
						//group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						//controllList.add(group);
				} catch (BotIssueTypeException e) {
						e.printStackTrace();
				} finally {
						issues2.clear();
				}

				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REORDER_MODIFIER);
				botIssue.setFilePath("classA");
				issues2.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REORDER_MODIFIER);
				botIssue.setFilePath("classA");
				issues2.add(botIssue);

				try {
						group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						group.addIssues(issues2);
						controllList.add(group);
						//group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						//controllList.add(group);
				} catch (BotIssueTypeException e) {
						e.printStackTrace();
				} finally {
						issues2.clear();
				}

				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_PARAMETER);
				botIssue.setFilePath("classB");
				issues2.add(botIssue);

				try {
						group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						group.addIssues(issues2);
						controllList.add(group);
						//group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						//controllList.add(group);
				} catch (BotIssueTypeException e) {
						e.printStackTrace();
				} finally {
						issues2.clear();
				}

				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssue.setFilePath("classC");
				issues2.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssue.setFilePath("classC");
				issues2.add(botIssue);

				try {
						group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						group.addIssues(issues2);
						controllList.add(group);
						//group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						//controllList.add(group);
				} catch (BotIssueTypeException e) {
						e.printStackTrace();
				} finally {
						issues2.clear();
				}

				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssue.setFilePath("classD");
				issues2.add(botIssue);
				botIssue = new BotIssue();
				botIssue.setRefactoringOperation(RefactoringOperations.REMOVE_COMMENTED_OUT_CODE);
				botIssue.setFilePath("classD");
				issues2.add(botIssue);

				try {
						group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						group.addIssues(issues2);
						controllList.add(group);
						//group = new BotIssueGroup(BotIssueGroupType.CLASS, issues2.get(0).getFilePath());
						//controllList.add(group);
				} catch (BotIssueTypeException e) {
						e.printStackTrace();
				} finally {
						issues2.clear();
				}
				System.out.println("group size: " + groups.size() + " controll size: " + controllList.size());
				assertThat(groups.size() == controllList.size());

				for (int i = 0; i < groups.size(); i++){
						System.out.println("group " + i + " type: " + groups.get(i).getType() + " controll type: " + controllList.get(i).getType());
						System.out.println("group list size: " + groups.get(i).getBotIssueGroup().size() + " controll list size: " + controllList.get(i).getBotIssueGroup().size());
						assertThat(groups.get(i).getBotIssueGroup().size() == controllList.get(i).getBotIssueGroup().size());

						for (int j = 0; j < groups.get(i).getBotIssueGroup().size(); j++){
								System.out.println("group " + i + " issue: " + j + " " + groups.get(i).getBotIssueGroup().get(j).getRefactoringOperation() + " controll group  " + i + " issue: " + j + " " + controllList.get(i).getBotIssueGroup().get(j).getRefactoringOperation());
								System.out.println("group " + i + " class: " + j + " " + groups.get(i).getBotIssueGroup().get(j).getFilePath() + " controll group  " + i + " class: " + j + " " + controllList.get(i).getBotIssueGroup().get(j).getFilePath());
								assertThat(groups.get(i).getBotIssueGroup().get(j).getRefactoringOperation()).isEqualTo(controllList.get(i).getBotIssueGroup().get(j).getRefactoringOperation());
								assertThat(groups.get(i).getBotIssueGroup().get(j).getFilePath()).isEqualTo(controllList.get(i).getBotIssueGroup().get(j).getFilePath());
						}
				}
		}
}
