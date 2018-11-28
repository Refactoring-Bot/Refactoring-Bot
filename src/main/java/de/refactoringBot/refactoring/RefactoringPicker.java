package de.refactoringBot.refactoring;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.refactoringBot.model.botIssue.BotIssue;
import de.refactoringBot.model.configuration.GitConfiguration;
import de.refactoringBot.refactoring.supportedRefactorings.AddOverrideAnnotation;
import de.refactoringBot.refactoring.supportedRefactorings.RemoveMethodParameter;
import de.refactoringBot.refactoring.supportedRefactorings.RenameMethod;
import de.refactoringBot.refactoring.supportedRefactorings.ReorderModifier;

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
	@Autowired
	RenameMethod renameMethod;
	@Autowired
	RemoveMethodParameter removeMethodParameter;

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

		// Pick refactoring class
		try {
			switch (issue.getRefactoringOperation()) {
			case "Add Override Annotation":
				return addOverride.performRefactoring(issue, gitConfig);
			case "Reorder Modifier":
				return reorderModifier.performRefactoring(issue, gitConfig);
			case "Rename Method":
			    return renameMethod.performRefactoring(issue, gitConfig);
			case "Remove Parameter":
				return removeMethodParameter.performRefactoring(issue, gitConfig);
			default:
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception("Something went wrong during the refactoring process!");
		}
	}
}
