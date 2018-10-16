package de.unistuttgart.iste.refactoringbot;

import java.io.FileNotFoundException;

import org.json.JSONObject;

/**
 * Interface that all refactoring classes should implement.
 */
public interface Refactoring {
	
	/**
	 * @return final commit message for the refactoring performed
	 */
	String getCommitMessage();
	
	/**
	 * 
	 * @param issue
	 * @param projectPath
	 * @throws FileNotFoundException 
	 */
	void performRefactoring(JSONObject issue, String projectPath) throws FileNotFoundException;
}
