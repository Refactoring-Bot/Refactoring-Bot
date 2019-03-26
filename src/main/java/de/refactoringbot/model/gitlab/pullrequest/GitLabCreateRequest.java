package de.refactoringbot.model.gitlab.pullrequest;

public class GitLabCreateRequest {

	private String source_branch;
	private String target_branch;
	private String title;
	private String description;
	private boolean allow_collaboration;
	
	public String getSource_branch() {
		return source_branch;
	}
	
	public void setSource_branch(String source_branch) {
		this.source_branch = source_branch;
	}
	
	public String getTarget_branch() {
		return target_branch;
	}
	
	public void setTarget_branch(String target_branch) {
		this.target_branch = target_branch;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public boolean isAllow_collaboration() {
		return allow_collaboration;
	}
	
	public void setAllow_collaboration(boolean allow_collaboration) {
		this.allow_collaboration = allow_collaboration;
	}
	
}
