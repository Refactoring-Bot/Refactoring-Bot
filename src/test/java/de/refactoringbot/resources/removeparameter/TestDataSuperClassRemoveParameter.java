package de.refactoringbot.resources.removeparameter;

public class TestDataSuperClassRemoveParameter {

	public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
		int result = (a + c) * 0 + 5;
		return result;
	}

}
