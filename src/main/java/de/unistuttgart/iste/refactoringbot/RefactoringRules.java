package de.unistuttgart.iste.refactoringbot;
/**
 * 
 * @author Timo Pfaff
 * 
 * this class defines constants, that represent the SonarQube rules.
 */

import java.util.HashMap;

import de.unistuttgart.iste.refactoringbot.refactorings.AddOverrideAnnotation;
import de.unistuttgart.iste.refactoringbot.refactorings.ReorderModifier;

public class RefactoringRules {
	
	public static final String REMOVE_UNUSED_VARIABLE = "squid:S1068";
	public static final String ADD_OVERRIDE_ANNOTATION = "squid:S1161";
	public static final String REMOVE_UNUSED_METHOD_PARAMETER = "squid:S1172";
	public static final String REORDER_MODIFIER = "squid:ModifiersOrderCheck";

	/**
	 * @return HashMap with sonarQube rule name as key and refactoring class which
	 *         can handle the rule
	 */
	public static HashMap<String, Class<?>> getSonarQubeRuleToRefactoringClassMapping() {
		HashMap<String, Class<?>> sonarQubeRuleToRefactoringClassMapping = new HashMap<>();
		sonarQubeRuleToRefactoringClassMapping.put(RefactoringRules.ADD_OVERRIDE_ANNOTATION, AddOverrideAnnotation.class);
		sonarQubeRuleToRefactoringClassMapping.put(RefactoringRules.REORDER_MODIFIER, ReorderModifier.class);
		
		return sonarQubeRuleToRefactoringClassMapping;
	}
	

	
}
