package de.refactoringbot.model.botissuegroup;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.exceptions.BotIssueTypeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is for the group of code smells that the refactoring bot will push
 */
public class BotIssueGroup {
	private BotIssueGroupType type;
	private List<BotIssue> botIssues;
	private String name = "";
	private int valueCounChange = 0;

	private static final Logger logger = LoggerFactory.getLogger(BotIssueGroup.class);

	public BotIssueGroup(BotIssueGroupType type) throws BotIssueTypeException {
		botIssues = new ArrayList<>();
		if (type == BotIssueGroupType.CLASS || type == BotIssueGroupType.REFACTORING
				|| type == BotIssueGroupType.OBJECT) {
			this.type = type;
		} else {
			throw new BotIssueTypeException("Invalid BotIssueType");
		}
	}

	public BotIssueGroup(BotIssueGroupType type, String name) throws BotIssueTypeException {
		this.name = name;
		botIssues = new ArrayList<>();
		if (type == BotIssueGroupType.CLASS || type == BotIssueGroupType.REFACTORING
				|| type == BotIssueGroupType.OBJECT) {
			this.type = type;
		} else {
			throw new BotIssueTypeException("Invalid BotIssueType");
		}
	}

	public void addIssue(BotIssue issue) {
		botIssues.add(issue);
	}

	public void addIssues(List<BotIssue> issues) {
		botIssues.addAll(issues);
	}

	public List<BotIssue> getBotIssueGroup() {
		return botIssues;
	}

	public void setType(BotIssueGroupType type) throws BotIssueTypeException {
		if (type == BotIssueGroupType.CLASS || type == BotIssueGroupType.REFACTORING
				|| type == BotIssueGroupType.OBJECT) {
			this.type = type;
		} else {
			throw new BotIssueTypeException("Invalid BotIssueType");
		}
	}

	public BotIssueGroupType getType() {
		return this.type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	// TODO: effizienter machen
	public int getValueCounChange() {

		for (BotIssue issue : botIssues) {
			valueCounChange += issue.getCountChanges();
		}

		return valueCounChange;
	}

	/**
	 * deletes an issue
	 * 
	 * @param issue
	 */
	public void remove(BotIssue issue) {
		try {
			botIssues.remove(issue);
		} catch (Exception e) {
			logger.error("Can't remove issue");
		}
	}
}
