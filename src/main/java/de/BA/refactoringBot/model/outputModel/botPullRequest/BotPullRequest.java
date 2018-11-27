package de.BA.refactoringBot.model.outputModel.botPullRequest;

import java.util.ArrayList;
import java.util.List;

import de.BA.refactoringBot.model.outputModel.botPullRequestComment.BotPullRequestComment;

public class BotPullRequest {

	private String requestName;
	private String requestDescription;
	private Integer requestNumber;
	private String requestStatus;
	private String creatorName;
	private String dateCreated;
	private String dateUpdated;
	private String branchName;
	private String branchCreator;
	private String mergeBranchName;
	private String repoName;
	private List<BotPullRequestComment> allComments = new ArrayList<BotPullRequestComment>(); 
	
	public String getRequestName() {
		return requestName;
	}
	
	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}
	
	public String getRequestDescription() {
		return requestDescription;
	}
	
	public void setRequestDescription(String requestDescription) {
		this.requestDescription = requestDescription;
	}
	
	public String getCreatorName() {
		return creatorName;
	}
	
	public void setCreatorName(String creatorName) {
		this.creatorName = creatorName;
	}
	
	public String getDateCreated() {
		return dateCreated;
	}
	
	public void setDateCreated(String dateCreated) {
		this.dateCreated = dateCreated;
	}
	
	public String getDateUpdated() {
		return dateUpdated;
	}
	
	public void setDateUpdated(String dateUpdated) {
		this.dateUpdated = dateUpdated;
	}
	
	public String getBranchName() {
		return branchName;
	}
	
	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}
	
	public String getMergeBranchName() {
		return mergeBranchName;
	}
	
	public void setMergeBranchName(String mergeBranchName) {
		this.mergeBranchName = mergeBranchName;
	}
	
	public String getRepoName() {
		return repoName;
	}
	
	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public Integer getRequestNumber() {
		return requestNumber;
	}

	public void setRequestNumber(Integer requestNumber) {
		this.requestNumber = requestNumber;
	}

	public String getRequestStatus() {
		return requestStatus;
	}

	public void setRequestStatus(String requestStatus) {
		this.requestStatus = requestStatus;
	}

	public List<BotPullRequestComment> getAllComments() {
		return allComments;
	}

	public void setAllComments(List<BotPullRequestComment> allComments) {
		this.allComments = allComments;
	}

	public String getBranchCreator() {
		return branchCreator;
	}

	public void setBranchCreator(String branchCreator) {
		this.branchCreator = branchCreator;
	}

}
