package de.refactoringbot.model.gitlab.pullrequest;

import java.util.ArrayList;
import java.util.List;

public class GitLabPullRequests {

	List<GitLabPullRequest> allPullRequests = new ArrayList<>();

	public List<GitLabPullRequest> getAllPullRequests() {
		return allPullRequests;
	}

	public void setAllPullRequests(List<GitLabPullRequest> allPullRequests) {
		this.allPullRequests = allPullRequests;
	}
	
	public void addPullRequest(GitLabPullRequest pullRequest) {
		this.allPullRequests.add(pullRequest);
	}
	
	public void removePullRequest(GitLabPullRequest pullRequest) {
		this.allPullRequests.remove(pullRequest);
	}
	
}
