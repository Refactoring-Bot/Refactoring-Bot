package de.refactoringbot.refactoring;

import de.refactoringbot.refactoring.supportedrefactorings.AddOverrideAnnotation;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveCommentedOutCode;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveMethodParameter;
import de.refactoringbot.refactoring.supportedrefactorings.RenameMethod;
import de.refactoringbot.refactoring.supportedrefactorings.ReorderModifier;
import org.junit.Test;

import java.util.Map;

import static de.refactoringbot.refactoring.RefactoringOperations.ADD_OVERRIDE_ANNOTATION;
import static de.refactoringbot.refactoring.RefactoringOperations.REMOVE_COMMENTED_OUT_CODE;
import static de.refactoringbot.refactoring.RefactoringOperations.REMOVE_PARAMETER;
import static de.refactoringbot.refactoring.RefactoringOperations.RENAME_METHOD;
import static de.refactoringbot.refactoring.RefactoringOperations.REORDER_MODIFIER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class RefactoringOperationsTest {

	@Test
	public void getRuleToClassMapping() {
		RefactoringOperations refactoringOperations = new RefactoringOperations();

		Map<String, Class<? extends RefactoringImpl>> ruleToClassMapping = refactoringOperations.getRuleToClassMapping();

		assertThat(ruleToClassMapping)
				.as("Add missing mappings of key to class to the test")
				.containsOnly(
				entry(ADD_OVERRIDE_ANNOTATION, AddOverrideAnnotation.class),
				entry(REORDER_MODIFIER, ReorderModifier.class),
				entry(RENAME_METHOD, RenameMethod.class),
				entry(REMOVE_COMMENTED_OUT_CODE, RemoveCommentedOutCode.class),
				entry(REMOVE_PARAMETER, RemoveMethodParameter.class));
	}
}