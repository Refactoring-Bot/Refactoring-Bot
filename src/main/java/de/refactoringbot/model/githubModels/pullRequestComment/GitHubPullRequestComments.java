package de.refactoringbot.model.githubModels.pullRequestComment;

import java.util.ArrayList;
import java.util.List;

public class GitHubPullRequestComments {

	List<PullRequestComment> comments = new ArrayList<>();

	public List<PullRequestComment> getComments() {
		return comments;
	}

	public void setComments(List<PullRequestComment> comments) {
		this.comments = comments;
	}
	
	public void addComment(PullRequestComment comment) {
		this.comments.add(comment);
	}
	
	public void removeComment(PullRequestComment comment) {
		this.comments.remove(comment);
	}
}
