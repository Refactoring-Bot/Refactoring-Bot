package de.refactoringbot.model.refactoredissue;

import de.refactoringbot.model.configuration.FileHoster;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "REFACTORED_ISSUES")
public class RefactoredIssue {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long issueId;

	private String commentServiceID;
	private String repoName;
	private String repoOwner;
	private FileHoster repoService;
	private String dateOfRefactoring;
	private String analysisService;
	private String analysisServiceProjectKey;
	private String refactoringOperation;
	private String status;

	public Long getIssueId() {
		return issueId;
	}

	public String getRepoName() {
		return repoName;
	}

	public void setRepoName(String repoName) {
		this.repoName = repoName;
	}

	public String getRepoOwner() {
		return repoOwner;
	}

	public void setRepoOwner(String repoOwner) {
		this.repoOwner = repoOwner;
	}

	public FileHoster getRepoService() {
		return repoService;
	}

	public void setRepoService(FileHoster repoService) {
		this.repoService = repoService;
	}

	public String getDateOfRefactoring() {
		return dateOfRefactoring;
	}

	public void setDateOfRefactoring(String dateOfRefactoring) {
		this.dateOfRefactoring = dateOfRefactoring;
	}

	public String getRefactoringOperation() {
		return refactoringOperation;
	}

	public void setRefactoringOperation(String refactoringOperation) {
		this.refactoringOperation = refactoringOperation;
	}

	public String getCommentServiceID() {
		return commentServiceID;
	}

	public void setCommentServiceID(String commentServiceID) {
		this.commentServiceID = commentServiceID;
	}

	public String getAnalysisService() {
		return analysisService;
	}

	public void setAnalysisService(String analysisService) {
		this.analysisService = analysisService;
	}

	public String getAnalysisServiceProjectKey() {
		return analysisServiceProjectKey;
	}

	public void setAnalysisServiceProjectKey(String analysisServiceProjectKey) {
		this.analysisServiceProjectKey = analysisServiceProjectKey;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
