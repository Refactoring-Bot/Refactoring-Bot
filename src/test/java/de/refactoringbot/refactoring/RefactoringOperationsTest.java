package de.refactoringbot.refactoring;

import de.refactoringbot.refactoring.supportedrefactorings.AddOverrideAnnotation;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveCommentedOutCode;
import de.refactoringbot.refactoring.supportedrefactorings.RemoveMethodParameter;
import de.refactoringbot.refactoring.supportedrefactorings.RenameMethod;
import de.refactoringbot.refactoring.supportedrefactorings.ReorderModifier;
import org.junit.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

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
						entry("Rename Method", ReorderModifier.class),
						entry("Reorder Modifier", RenameMethod.class),
						entry("Remove Commented Out Code", RemoveCommentedOutCode.class),
						entry("Remove Parameter", RemoveMethodParameter.class));
	}
}