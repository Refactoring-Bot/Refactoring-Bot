package de.refactoringbot.model.github.pullrequest;

public class GithubUpdateRequest {

	private String body;
	private boolean maintainer_can_modify;
	
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
