package de.refactoringbot.model.botissue;

import java.util.List;

//TODO: datum und anzahl änderungen evtl adden
public class BotIssue {

	private String refactoringOperation;
	private String filePath;
	private Integer line;
	private String commentServiceID;
	private String refactorString;
	private String errorMessage;
	private String commitMessage;
	private List<String> allJavaFiles;
	private List<String> javaRoots;
	private int countChanges;

	/**
	 * The refactoring operation describes the refactoring that will be performed.
	 * It is used to map the BotIssue to a refactoring class that is used for
	 * refactoring.
	 * 
	 * @return refactoringOperation
	 */
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

	/**
	 * This method returns an refactoring string. A refactoring string is something
	 * like a new name of a method (used for renaming) or the name of an parameter
	 * that needs to be removed.
	 * 
	 * @return refactorString
	 */
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

	public List<String> getAllJavaFiles() {
		return allJavaFiles;
	}

	public void setAllJavaFiles(List<String> allJavaFiles) {
		this.allJavaFiles = allJavaFiles;
	}

	/**
	 * This method returns the java roots. A java root folder is the root folder of
	 * an java file. An example would be the src folder that is commonly used in
	 * java projects.
	 * 
	 * @return javaRoots
	 */
	public List<String> getJavaRoots() {
		return javaRoots;
	}

	public void setJavaRoots(List<String> javaRoots) {
		this.javaRoots = javaRoots;
	}

	public void setCountChanges(int count){this.countChanges = count;}

	public int getCountChanges(){return countChanges;}

}
