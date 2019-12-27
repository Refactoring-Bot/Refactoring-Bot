package de.refactoringbot.model.botissuegroup;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.exceptions.BotIssueTypeException;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for the group of code smells that the refactoring bot will push
 * TODO: in JSON oder DB speichern?
 */
public class BotIssueGroup {
		//TODO: type in enum oder so speichern
		private BotIssueGroupType type;
		private List<BotIssue> botIssues;

		public BotIssueGroup(BotIssueGroupType type) throws BotIssueTypeException {
				botIssues = new ArrayList<>();
				if (type == BotIssueGroupType.CLASS || type == BotIssueGroupType.REFACTORING || type == BotIssueGroupType.OBJECT){
						this.type = type;
				}else{
						throw new BotIssueTypeException("Invalid BotIssueType");
				}
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

		public void setType(BotIssueGroupType type) throws BotIssueTypeException {
				if (type == BotIssueGroupType.CLASS || type == BotIssueGroupType.REFACTORING || type == BotIssueGroupType.OBJECT){
						this.type = type;
				}else{
						throw new BotIssueTypeException("Invalid BotIssueType");
				}
		}

		public BotIssueGroupType getType(){return this.type;}
}
