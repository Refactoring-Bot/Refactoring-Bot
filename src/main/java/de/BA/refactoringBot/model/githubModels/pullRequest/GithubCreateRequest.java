package de.BA.refactoringBot.model.githubModels.pullRequest;

public class GithubCreateRequest {

	private String title;
	private String head;
	private String base;
	private String body;
	private boolean maintainer_can_modify;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getHead() {
		return head;
	}
	public void setHead(String head) {
		this.head = head;
	}
	public String getBase() {
		return base;
	}
	public void setBase(String base) {
		this.base = base;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public boolean isMaintainer_can_modify() {
		return maintainer_can_modify;
	}
	public void setMaintainer_can_modify(boolean maintainer_can_modify) {
		this.maintainer_can_modify = maintainer_can_modify;
	}

}
