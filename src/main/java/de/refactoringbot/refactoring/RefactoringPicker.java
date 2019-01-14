package de.refactoringbot.refactoring;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringbot.model.botissue.BotIssue;
import de.refactoringbot.model.configuration.GitConfiguration;
import de.refactoringbot.model.exceptions.BotRefactoringException;

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

	private static final Logger logger = LoggerFactory.getLogger(RefactoringPicker.class);

	/**
	 * This method checks which refactoring needs to be performed. It transfers the
	 * refactoring request to the correct refactoring class and returns a commit
	 * message.
	 * 
	 * @param issue
	 * @return commitMessage
	 * @throws BotRefactoringException
	 */
	public String pickAndRefactor(BotIssue issue, GitConfiguration gitConfig) throws BotRefactoringException {

		try {
			// Get rule to class mapping
			HashMap<String, Class<?>> ruleToClassMapping = operations.getRuleToClassMapping();
			// Get class of the mapping
			Class<?> refactoringClass = ruleToClassMapping.get(issue.getRefactoringOperation());

			// If class for refactoring exists
			if (refactoringClass != null) {
				Constructor<?> ctor = refactoringClass.getConstructor();
				Object object = ctor.newInstance(new Object[] {});
				return (String) refactoringClass.getMethod("performRefactoring", BotIssue.class, GitConfiguration.class)
						.invoke(object, issue, gitConfig);
			} else {
				throw new BotRefactoringException("Bot does not support specified refactoring yet!");
			}
		} catch (Exception e) {
			if (e.getCause() != null) {
				logger.error(e.getCause().getMessage(), e.getCause());
				throw new BotRefactoringException(e.getCause().getMessage());
			} else {
				logger.error(e.getMessage(), e);
				throw new BotRefactoringException(e.getMessage());
			}

		}
	}
}
