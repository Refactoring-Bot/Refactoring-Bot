package de.refactoringBot.refactoring;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringBot.configuration.BotConfiguration;
import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;

/**
 * This class checks which refactoring needs to be performed.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class RefactoringPicker {

	@Autowired
	RefactoringOperations operations;
	@Autowired
	BotConfiguration botConfig;

	/**
	 * This method checks which refactoring needs to be performed. It transfers the
	 * refactoring request to the correct refactoring class and returns a commit
	 * message.
	 * 
	 * @param issue
	 * @return commitMessage
	 * @throws Exception
	 */
	public String pickAndRefactor(BotIssue issue, GitConfiguration gitConfig) throws Exception {

		try {
			// Get rule to class mapping
			HashMap<String, Class<?>> ruleToClassMapping = operations.getRuleToClassMapping();
			// Get class of the mapping
			Class<?> refactoringClass = ruleToClassMapping.get(issue.getRefactoringOperation());

			// If class for refactoring exists
			if (refactoringClass != null) {
				Constructor<?> ctor = refactoringClass.getConstructor();
				Object object = ctor.newInstance(new Object[] {});
				return (String) refactoringClass.getMethod("performRefactoring", BotIssue.class, GitConfiguration.class, BotConfiguration.class).invoke(object, issue, gitConfig, botConfig);
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Something went wrong during the refactoring process!");
		}
	}
}
