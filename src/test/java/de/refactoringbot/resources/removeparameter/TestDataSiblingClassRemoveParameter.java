package de.refactoringbot.resources.removeparameter;

public class TestDataSiblingClassRemoveParameter extends TestDataSuperClassRemoveParameter {

	@Override
	public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
		int result = (a + c) * 0;
		result = 6;
		return result;
	}

	public int getLineNumberOfCaller() {
		getLineOfMethodWithUnusedParameter(1, 2, 3);
		return 12;
	}

}
