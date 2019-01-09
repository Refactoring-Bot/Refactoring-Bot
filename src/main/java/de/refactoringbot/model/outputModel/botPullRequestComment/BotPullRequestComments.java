package de.refactoringbot.model.outputModel.botPullRequestComment;

import java.util.ArrayList;
import java.util.List;

public class BotPullRequestComments {
	List<BotPullRequestComment> comments = new ArrayList<>();

	public List<BotPullRequestComment> getComments() {
		return comments;
	}

	public void setComments(List<BotPullRequestComment> comments) {
		this.comments = comments;
	}
	
	public void addComment(BotPullRequestComment comment) {
		this.comments.add(comment);
	}
	
	public void removeComment(BotPullRequestComment comment) {
		this.comments.remove(comment);
	}
}
