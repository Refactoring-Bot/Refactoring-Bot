package de.refactoringbot.resources.removeparameter;

public class TestDataClassImplementingEmptyInterface implements TestDataEmptyInterface {

	public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
		return 5;
	}
	
	public class TestDataInnerClassImplementingEmptyInterface implements TestDataEmptyInterface {
		public int getLineOfMethodWithUnusedParameter(int a, int b, int c) {
			return 10;
		}
	}

}
