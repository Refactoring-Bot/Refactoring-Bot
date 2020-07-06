package de.refactoringbot.refactoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.Map;

import org.junit.Test;

import de.refactoringbot.refactoring.supportedrefactorings.AddOverrideAnnotation;
import de.refactoringbot.refactoring.supportedrefactorings.ImmediatelyReturnExpression;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveCommentedOutCode;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveMethodParameter;
import de.refactoringbot.refactoring.supportedrefactorings.RenameMethod;
import de.refactoringbot.refactoring.supportedrefactorings.ReorderModifier;

public class RefactoringOperationsTest {

	@Test
	public void getRuleToClassMapping() {
		RefactoringOperations refactoringOperations = new RefactoringOperations();

		Map<String, Class<? extends RefactoringImpl>> ruleToClassMapping = refactoringOperations.getRuleToClassMapping();

		assertThat(ruleToClassMapping)
				.as("Add missing mappings of key to class to the test. "
						+ "Keys should not be renamed without migration as they are persisted to the database")
				.containsOnly(
						entry("Add Override Annotation", AddOverrideAnnotation.class),
						entry("Rename Method", RenameMethod.class),
						entry("Reorder Modifier", ReorderModifier.class),
						entry("Remove Commented Out Code", RemoveCommentedOutCode.class),
						entry("Remove Parameter", RemoveMethodParameter.class),
						entry("Immediately return expression", ImmediatelyReturnExpression.class));
	}
}