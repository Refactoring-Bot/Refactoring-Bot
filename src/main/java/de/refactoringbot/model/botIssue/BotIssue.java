package de.refactoringbot.model.botIssue;

import java.util.List;

public class BotIssue {

	private String refactoringOperation;
	private String filePath;
	private Integer line;
	private String commentServiceID;
	private String refactorString;
	private String errorMessage;
	private String commitMessage;
	private String creationDate;
	private List<String> allJavaFiles;
	private List<String> javaRoots;

	public String getRefactoringOperation() {
		return refactoringOperation;
	}

	public void setRefactoringOperation(String refactoringOperation) {
		this.refactoringOperation = refactoringOperation;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(Integer line) {
		this.line = line;
	}

	public String getCommentServiceID() {
		return commentServiceID;
	}

	public void setCommentServiceID(String commentServiceID) {
		this.commentServiceID = commentServiceID;
	}

	public String getRefactorString() {
		return refactorString;
	}

	public void setRefactorString(String refactorString) {
		this.refactorString = refactorString;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getCommitMessage() {
		return commitMessage;
	}

	public void setCommitMessage(String commitMessage) {
		this.commitMessage = commitMessage;
	}

	public String getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(String creationDate) {
		this.creationDate = creationDate;
	}

	public List<String> getAllJavaFiles() {
		return allJavaFiles;
	}

	public void setAllJavaFiles(List<String> allJavaFiles) {
		this.allJavaFiles = allJavaFiles;
	}

	public List<String> getJavaRoots() {
		return javaRoots;
	}

	public void setJavaRoots(List<String> javaRoots) {
		this.javaRoots = javaRoots;
	}

}
