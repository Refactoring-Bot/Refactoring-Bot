package de.refactoringbot.resources.removeparameter;

public class TestDataSubClassRemoveParameter extends TestDataClassRemoveParameter {

	@Override
	public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
		int result = (a + c) * 0 + 6;
		return result;
	}

}
