package de.refactoringbot.model.githubModels.pullRequest;

import java.util.ArrayList;
import java.util.List;

public class GithubPullRequests {

	List<GithubPullRequest> allPullRequests = new ArrayList<>();

	public List<GithubPullRequest> getAllPullRequests() {
		return allPullRequests;
	}

	public void setAllPullRequests(List<GithubPullRequest> allPullRequests) {
		this.allPullRequests = allPullRequests;
	}
	
	public void addPullRequest(GithubPullRequest pullRequest) {
		this.allPullRequests.add(pullRequest);
	}
	
	public void removePullRequest(GithubPullRequest pullRequest) {
		this.allPullRequests.remove(pullRequest);
	}
	
}
