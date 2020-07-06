package de.refactoringbot.resources.reordermodifiers;

public class TestDataClassReorderModifiers {

	public final static int lineNumberOfFieldWithStaticAndFinalInWrongOrder = 5;
	public final static String lineNumberOfStringFieldWithStaticAndFinalInWrongOrder = "6";

	public static final synchronized int getLineOfMethodWithAllModifiersInCorrectOrder() {
		return 8;
	}

	public final static int getLineOfMethodWithStaticAndFinalInWrongOrder() {
		return 12;
	}

	public final static String getLineOfStringMethodWithStaticAndFinalInWrongOrder() {
		return "16";
	}

}
