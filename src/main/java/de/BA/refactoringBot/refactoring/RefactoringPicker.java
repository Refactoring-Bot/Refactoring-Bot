package de.BA.refactoringBot.refactoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.BA.refactoringBot.model.botIssue.BotIssue;
import de.BA.refactoringBot.model.configuration.GitConfiguration;
import de.BA.refactoringBot.refactoring.supportedRefactorings.AddOverrideAnnotation;
import de.BA.refactoringBot.refactoring.supportedRefactorings.ReorderModifier;

/**
 * This class checks which refactoring needs to be performed.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class RefactoringPicker {

	@Autowired
	AddOverrideAnnotation addOverride;
	@Autowired
	ReorderModifier reorderModifier;

	/**
	 * This method checks which refactoring needs to be performed. It transfers the
	 * refactoring request to the correct refactoring class and returns a commit
	 * message.
	 * 
	 * @param issue
	 * @return commitMessage
	 * @throws Exception
	 */
	public String pickRefactoring(BotIssue issue, GitConfiguration gitConfig) throws Exception {

		// Pick refactoring class
		try {
			switch (issue.getRefactoringOperation()) {
			case "Add Override Annotation":
				return addOverride.performRefactoring(issue, gitConfig);
			case "Reorder Modifier":
				return reorderModifier.performRefactoring(issue, gitConfig);
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Something went wrong during the refactoring process!");
		}
	}
}
