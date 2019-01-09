package de.refactoringbot.model.githubModels.pullRequestComment;

public class ReplyComment {

	private String body;
	private Integer in_reply_to;
	
	public String getBody() {
		return body;
	}
	
	public void setBody(String body) {
		this.body = body;
	}
	
	public Integer getIn_reply_to() {
		return in_reply_to;
	}
	
	public void setIn_reply_to(Integer in_reply_to) {
		this.in_reply_to = in_reply_to;
	}
}
