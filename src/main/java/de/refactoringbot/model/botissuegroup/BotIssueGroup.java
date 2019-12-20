package de.refactoringbot.model.botissuegroup;

import de.refactoringbot.model.botissue.BotIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for the group of code smells that the refactoring bot will push
 * TODO: in JSON oder DB speichern?
 */
public class BotIssueGroup {
		private List<BotIssue> botIssues;

		public BotIssueGroup(){
				botIssues = new ArrayList<>();
		}

		public void addIssue(BotIssue issue){
				botIssues.add(issue);
		}

		public void addIssues(List<BotIssue> issues){
				botIssues.addAll(issues);
		}

		public List<BotIssue> getBotIssueGroup(){
				return botIssues;
		}
}
