package de.refactoringbot.model.gitlab.pullrequestdiscussion;

import java.util.ArrayList;
import java.util.List;

public class GitLabDiscussions {

	List<GitLabDiscussion> discussions = new ArrayList<>();
	
	public List<GitLabDiscussion> getDiscussions() {
		return discussions;
	}

	public void setDiscussions(List<GitLabDiscussion> discussions) {
		this.discussions = discussions;
	}
	
	public void addDiscussion(GitLabDiscussion discussion) {
		this.discussions.add(discussion);
	}
	
	public void removeDiscussion(GitLabDiscussion discussion) {
		this.discussions.remove(discussion);
	}
}
