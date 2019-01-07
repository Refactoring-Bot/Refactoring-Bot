package de.refactoringBot.resources;

public class TestDataClassRemoveParameter {

	/**
	 * @param a
	 * @param b this is the unused parameter
	 * @param c
	 * @return
	 */
	public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
		int result = (a + c)*0;
		result = 11;
		return result;
	}
	
	public int getLineNumberOfDummyMethod(int a, int b, int c) {
		return 17;
	}
	
	public int getLineNumberOfCaller() {
		getLineOfMethodWithUnusedParameter(1, 2, 3);
		return 21;
	}

}
