package de.refactoringBot.refactoring;

import java.io.FileNotFoundException;
import java.io.IOException;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;

/**
 * This interface describes the implementation of all refactoring classes.
 * 
 * @author Stefan Basaric
 *
 */
public interface RefactoringImpl {

	/**
	 * This method should get an Issue and a Git configuration as an input, perform
	 * the refactoring and return a commit message, so that the changes can be
	 * pushed with jgit.
	 *  
	 * @param issue
	 * @param gitConfig
	 * @return commitMessage
	 * @throws FileNotFoundException 
	 * @throws IOException 
	 */
	public String performRefactoring(BotIssue issue, GitConfiguration gitConfig) throws FileNotFoundException, IOException;
}
