package de.refactoringbot.model.output.botpullrequestcomment;

public class BotPullRequestComment {

	private String discussionID;
	private Integer commentID;
	private String username;
	private String filepath;
	private Integer position;
	private String commentBody;
	
	public String getDiscussionID() {
		return discussionID;
	}

	public void setDiscussionID(String discussionID) {
		this.discussionID = discussionID;
	}

	public Integer getCommentID() {
		return commentID;
	}

	public void setCommentID(Integer commentID) {
		this.commentID = commentID;
	}

	public String getFilepath() {
		return filepath;
	}

	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}

	public Integer getPosition() {
		return position;
	}

	public void setPosition(Integer position) {
		this.position = position;
	}

	public String getCommentBody() {
		return commentBody;
	}

	public void setCommentBody(String commentBody) {
		this.commentBody = commentBody;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}
