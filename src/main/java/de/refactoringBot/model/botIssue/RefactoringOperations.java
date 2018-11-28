package de.refactoringBot.model.botIssue;

import org.springframework.stereotype.Component;

@Component
public class RefactoringOperations {
	
	public final String addOverrideAnnotation = "Add Override Annotation";
	public final String renameMethod = "Rename Method";
	public final String reorderModifier = "Reorder Modifier";
	public final String removeParameter = "Remove Parameter";
	public final String unknownRefactoring = "Unknown Refactoring";

}
