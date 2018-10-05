package main.java.de.unistuttgart.iste.refactoringbot;
/**
 * 
 * @author Timo Pfaff
 * 
 * this class defines constants, that represent the SonarQube rules.
 */
public class RefactoringRules {
	
	public static final String REMOVE_UNUSED_VARIABLE = "squid:S1068";
	public static final String ADD_OVERRIDE_ANNOTATION = "squid:S1161";
	public static final String REMOVE_UNUSED_METHOD_PARAMETER = "squid:S1172";
	public static final String REORDER_MODIFIER = "squid:ModifiersOrderCheck";

}
