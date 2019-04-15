package de.refactoringbot.resources.immediatelyreturn;

public class TestDataClassImmediatelyReturnExpression {

	private static int a = 0;

	public static int dummyMethod() {
		return 0;
	}

	public static int getLineOfResultAssignment() {
		int result = 12 + 0;
		return result;
	}

	public static int getLineOfResultAssignmentWithCommentLineAfter() {
		int result = 17 + 0;
		// comment
		return result;
	}

	public static int getLineOfSecondResultAssignment() {
		int aDoubled = a * 2;
		if (aDoubled != 0) {
			return 0;
		}

		int result = 28 + 0;
		return result;
	}

	public static int getLineOfAssignmentUsedMoreThanOnce() {
		int result = 33 + 0;
		if (Math.random() > 0) {
			return result;
		}

		return result;
	}

	public static int getLineOfComplexResultAssignment() {
		int result = 42 + (dummyMethod() * a);
		return result;
	}

	public static int getLineOfResultAssignmentSpanningMultipleLines() {
		int result = 47 + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod()
				+ dummyMethod() + dummyMethod() + dummyMethod() + dummyMethod();
		return result;
	}

	public static int getLineOfResultAssignmentWithLineComment() {
		int result = 53; // comment
		return result;
	}

}
