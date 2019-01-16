package de.refactoringbot.resources.reordermodifiers;

public class TestDataClassReorderModifiers {

	public final static int lineNumberOfFieldWithStaticAndFinalInWrongOrder = 5;

	public static final synchronized int getLineOfMethodWithAllModifiersInCorrectOrder() {
		return 7;
	}

	public final static int getLineOfMethodWithStaticAndFinalInWrongOrder() {
		return 11;
	}

}
