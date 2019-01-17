package de.refactoringbot.refactoring;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import de.refactoringbot.refactoring.supportedrefactorings.AddOverrideAnnotation;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveCommentedOutCode;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveMethodParameter;
import de.refactoringbot.refactoring.supportedrefactorings.RenameMethod;
import de.refactoringbot.refactoring.supportedrefactorings.ReorderModifier;

/**
 * This class holds all supported refactoring operations and maps them to a
 * refactoring class which performs that specific refactoring.
 * 
 * @author Stefan Basaric
 *
 */
@Component
public class RefactoringOperations {

	public static final String ADD_OVERRIDE_ANNOTATION = "Add Override Annotation";
	public static final String RENAME_METHOD = "Rename Method";
	public static final String REORDER_MODIFIER = "Reorder Modifier";
	public static final String REMOVE_COMMENTED_OUT_CODE = "Remove Commented Out Code";
	public static final String REMOVE_PARAMETER = "Remove Parameter";
	public static final String UNKNOWN = "Unknown Refactoring";

	/**
	 * This method maps a specific refactoring to a java class which can perform
	 * that refactoring.
	 * 
	 * @return
	 */
	public Map<String, Class<? extends RefactoringImpl>> getRuleToClassMapping() {
		HashMap<String, Class<? extends RefactoringImpl>> ruleToClassMapping = new HashMap<>();
		ruleToClassMapping.put(ADD_OVERRIDE_ANNOTATION, AddOverrideAnnotation.class);
		ruleToClassMapping.put(REORDER_MODIFIER, ReorderModifier.class);
		ruleToClassMapping.put(RENAME_METHOD, RenameMethod.class);
		ruleToClassMapping.put(REMOVE_COMMENTED_OUT_CODE, RemoveCommentedOutCode.class);
		ruleToClassMapping.put(REMOVE_PARAMETER, RemoveMethodParameter.class);

		return ruleToClassMapping;
	}

}
