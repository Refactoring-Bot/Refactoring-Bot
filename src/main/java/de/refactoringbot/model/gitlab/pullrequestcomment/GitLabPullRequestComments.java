package de.refactoringbot.model.gitlab.pullrequestcomment;

import java.util.ArrayList;
import java.util.List;

public class GitLabPullRequestComments {

	List<GitLabPullRequestComment> comments = new ArrayList<GitLabPullRequestComment>();
	
	public List<GitLabPullRequestComment> getComments() {
		return comments;
	}

	public void setComments(List<GitLabPullRequestComment> comments) {
		this.comments = comments;
	}
	
	public void addComment(GitLabPullRequestComment comment) {
		this.comments.add(comment);
	}
	
	public void removeComment(GitLabPullRequestComment comment) {
		this.comments.remove(comment);
	}
}
