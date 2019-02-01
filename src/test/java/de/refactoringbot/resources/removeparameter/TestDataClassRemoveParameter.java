package de.refactoringbot.resources.removeparameter;

public class TestDataClassRemoveParameter extends TestDataSuperClassRemoveParameter {

	/**
	 * @param a
	 * @param b
	 *            this is the unused parameter
	 * @param c
	 * @return
	 */
	@Override
	public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
		int result = (a + c) * 0;
		result = 13;

		// call another method with same parameter name 'b'. Unused parameter should
		// still be removed. Called method should remain unchanged
		getLineOfMethodWithSameParameterName(2);

		return result;
	}

	public int getLineNumberOfDummyMethod(int a, int b, int c) {
		return 24;
	}

	public int getLineNumberOfCaller() {
		getLineOfMethodWithUnusedParameter(1, 2, 3);
		return 28;
	}

	public int getLineOfMethodWithSameParameterName(int b) {
		return (b * 0) + 33;
	}

}
