package de.refactoringbot.model.refactoredissuegroup;

import de.refactoringbot.model.refactoredissue.RefactoredIssue;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is to stor the refactored issues in groups so that the
 * refactoredIssueGroup will be the same as the botIssueGroup
 */
public class RefactoredIssueGroup {
	private List<RefactoredIssue> refactoredIssues;

	public RefactoredIssueGroup() {
		refactoredIssues = new ArrayList<>();
	}

	public void addIssue(RefactoredIssue issue) {
		refactoredIssues.add(issue);
	}

	public void addIssues(List<RefactoredIssue> issues) {
		refactoredIssues.addAll(issues);
	}

	public List<RefactoredIssue> getRefactoredIssueGroup() {
		return refactoredIssues;
	}
}
